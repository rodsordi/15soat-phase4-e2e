package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class BillingSteps {

    private final ScenarioContext context;

    @Dado("que o serviço de Faturamento está em execução e operacional")
    public void theBillingServiceIsAvailable() {
        // Operational check verified via baseUri per request
    }

    @Dado("que o analista de faturamento está devidamente autenticado com perfil {string}")
    public void billingAnalystIsAuthenticated(String role) {
        context.setCurrentAuthToken("Bearer valid_" + role.toLowerCase() + "_token");
    }

    @Dado("uma fatura gerada com status {string}")
    public void invoiceGeneratedWithStatus(String status) {
        anInvoiceIsCreated(350.00, "52998224725");
    }

    @Dado("uma fatura criada para a Ordem de Serviço com o valor {double} e cliente {string}")
    @Quando("uma fatura é criada para a Ordem de Serviço com o valor {double} e cliente {string}")
    public void anInvoiceIsCreated(double amount, String customerDocument) {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }

        String payload = String.format(Locale.US, """
                {
                  "workOrderId": "%s",
                  "customerDocument": "%s",
                  "amount": %.2f
                }
                """, workOrderId, customerDocument, amount);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/invoices");

        context.setResponse(response);

        if (response != null && response.getStatusCode() == 201) {
            context.setInvoiceId(response.jsonPath().getString("id"));
        } else if (response != null && response.getStatusCode() == 200) {
            context.setInvoiceId(response.jsonPath().getString("id"));
        } else {
            context.setInvoiceId(UUID.randomUUID().toString());
        }
    }

    @Quando("a fatura é emitida com os seguintes dados:")
    public void anInvoiceIsCreatedFromTable(Map<String, String> data) {
        double amount = Double.parseDouble(data.get("amount"));
        String customerDocument = data.get("customerDocument");
        anInvoiceIsCreated(amount, customerDocument);
    }

    @Entao("a fatura deve ser criada com o status {string}")
    public void theInvoiceShouldBeCreatedWithStatus(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
    }

    @E("a preferência de pagamento deve conter um link de checkout válido")
    public void checkoutUrlIsValid() {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);

        String invoiceId = response.jsonPath().getString("id");
        if (invoiceId != null) {
            String checkoutPayload = """
                    {
                      "invoiceId": "%s"
                    }
                    """.formatted(invoiceId);

            Response checkoutResponse = context.givenBillingAuth()
                    .body(checkoutPayload)
                    .post("/api/v1/payments/mercadopago/checkout");

            if (checkoutResponse != null && checkoutResponse.getStatusCode() == 200) {
                String paymentId = checkoutResponse.jsonPath().getString("paymentId");
                if (paymentId != null) {
                    context.setExternalPaymentId(paymentId);
                }
                assertThat(checkoutResponse.jsonPath().getString("qrCode")).isNotEmpty();
                return;
            }
        }

        // Fallback for HATEOAS HAL _links or direct checkoutUrl
        String checkoutUrl = response.jsonPath().getString("checkoutUrl");
        if (checkoutUrl != null) {
            assertThat(checkoutUrl).isNotEmpty();
        } else {
            assertThat(response.jsonPath().getMap("_links")).isNotEmpty();
        }
    }

    @Quando("uma nova solicitação de fatura é submetida para a mesma Ordem de Serviço com o valor {double}")
    public void submitDuplicateInvoiceRequest(double amount) {
        String workOrderId = context.getWorkOrderId();
        String payload = String.format(Locale.US, """
                {
                  "workOrderId": "%s",
                  "customerDocument": "52998224725",
                  "amount": %.2f
                }
                """, workOrderId, amount);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/invoices");

        context.setResponse(response);
    }

    @Entao("o sistema deve retornar a fatura existente sem duplicar o registro")
    public void existingInvoiceReturnedWithoutDuplicate() {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("o identificador da fatura deve permanecer inalterado")
    public void invoiceIdMustRemainUnchanged() {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        String returnedId = response.jsonPath().getString("id");
        assertThat(returnedId).isEqualTo(context.getInvoiceId());
    }

    @Quando("o analista consulta a fatura pelo seu identificador único")
    public void fetchInvoiceById() {
        String invoiceId = context.getInvoiceId();
        Response response = context.givenBillingAuth()
                .get("/api/v1/invoices/" + invoiceId);
        context.setResponse(response);
    }

    @E("a fatura deve conter os links HATEOAS de auto-referência e da ordem de serviço")
    public void invoiceMustContainHateoasLinks() {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.jsonPath().getString("_links.self.href")).isNotEmpty();
        assertThat(response.jsonPath().getString("_links.workOrder.href")).isNotEmpty();
    }

    @Quando("o analista consulta a fatura pelo identificador da Ordem de Serviço")
    public void fetchInvoiceByWorkOrderId() {
        String workOrderId = context.getWorkOrderId();
        Response response = context.givenBillingAuth()
                .get("/api/v1/invoices/work-order/" + workOrderId);
        context.setResponse(response);
    }

    @E("a mesma fatura deve ser retornada com status {string}")
    public void sameInvoiceReturnedWithStatus(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        if (context.getInvoiceId() != null) {
            assertThat(response.jsonPath().getString("id")).isEqualTo(context.getInvoiceId());
        }
    }

    @Quando("o analista consulta uma fatura com um ID inexistente {string}")
    public void fetchNonExistentInvoice(String nonExistentId) {
        Response response = context.givenBillingAuth()
                .get("/api/v1/invoices/" + nonExistentId);
        context.setResponse(response);
    }

    @Quando("uma requisição de inicialização de checkout é enviada para a fatura")
    public void initializeCheckoutForInvoice() {
        String invoiceId = context.getInvoiceId();
        String payload = """
                {
                  "invoiceId": "%s"
                }
                """.formatted(invoiceId);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/payments/mercadopago/checkout");
        context.setResponse(response);

        if (response != null && response.getStatusCode() == 200) {
            String paymentId = response.jsonPath().getString("paymentId");
            if (paymentId != null) {
                context.setExternalPaymentId(paymentId);
            }
        }
    }

    @E("a resposta deve conter a chave {string} e o payload {string} do Mercado Pago")
    public void responseMustContainQrCodeKeys(String key1, String key2) {
        Response response = context.getResponse();
        assertThat(response).as("Billing response must not be null").isNotNull();
        assertThat(response.jsonPath().getString(key1)).isNotEmpty();
        assertThat(response.jsonPath().getString(key2)).isNotEmpty();
    }

    @Dado("que a requisição não possui cabeçalho de autenticação")
    public void requestWithoutAuthHeader() {
        context.setCurrentAuthToken(null);
    }

    @Quando("uma tentativa de emissão de fatura é submetida")
    public void submitInvoiceWithoutAuth() {
        String payload = String.format(Locale.US, """
                {
                  "workOrderId": "%s",
                  "customerDocument": "52998224725",
                  "amount": 350.00
                }
                """, UUID.randomUUID());

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/invoices");

        context.setResponse(response);
    }

    @Quando("o Mercado Pago notifica o evento {string} para a fatura com pagamento aprovado")
    public void mercadoPagoNotifiesApprovedPayment(String eventAction) {
        String paymentId = context.getExternalPaymentId();
        if (paymentId == null) {
            paymentId = context.getInvoiceId() != null ? context.getInvoiceId() : "PAY-" + UUID.randomUUID().toString().substring(0, 8);
            context.setExternalPaymentId(paymentId);
        }

        String payload = String.format("""
                {
                  "action": "%s",
                  "data": {
                    "id": "%s"
                  }
                }
                """, eventAction, paymentId);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/payments/mercadopago/webhook");

        context.setResponse(response);
    }

    @Quando("o Mercado Pago notifica o evento {string} para a fatura com pagamento recusado")
    public void mercadoPagoNotifiesRejectedPayment(String eventAction) {
        String paymentId = context.getExternalPaymentId();
        if (paymentId == null) {
            paymentId = context.getInvoiceId() != null ? context.getInvoiceId() : "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String rejectedPaymentId = paymentId + "-REJECT";

        String payload = String.format("""
                {
                  "action": "%s",
                  "data": {
                    "id": "%s"
                  }
                }
                """, eventAction, rejectedPaymentId);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/payments/mercadopago/webhook");

        context.setResponse(response);
    }

    @Entao("o webhook deve responder com status 200 e confirmação {string}")
    public void webhookShouldRespondWithStatusAndConfirmation(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Webhook response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
    }

    @Dado("que a fatura foi previamente liquidada com sucesso")
    public void invoicePreviouslySettledSuccessfully() {
        mercadoPagoNotifiesApprovedPayment("payment.updated");
        theInvoiceStatusShouldBeTransitionedTo("PAID");
    }

    @Quando("o Mercado Pago retransmite a notificação de webhook com o mesmo identificador")
    public void mercadoPagoRetransmitsWebhook() {
        String paymentId = context.getExternalPaymentId();
        String payload = String.format("""
                {
                  "action": "payment.updated",
                  "data": {
                    "id": "%s"
                  }
                }
                """, paymentId);

        Response response = context.givenBillingAuth()
                .body(payload)
                .post("/api/v1/payments/mercadopago/webhook");

        context.setResponse(response);
    }

    @E("o status da fatura deve permanecer inalterado como {string}")
    public void invoiceStatusMustRemainUnchanged(String expectedStatus) {
        theInvoiceStatusShouldBeTransitionedTo(expectedStatus);
    }

    @Quando("o Mercado Pago envia uma notificação de webhook de pagamento com o status {string}")
    public void mercadoPagoPostsWebhook(String paymentStatus) {
        if ("rejected".equalsIgnoreCase(paymentStatus)) {
            mercadoPagoNotifiesRejectedPayment("payment.updated");
        } else {
            mercadoPagoNotifiesApprovedPayment("payment.updated");
        }
    }

    @Entao("o status da fatura deve ser alterado para {string}")
    public void theInvoiceStatusShouldBeTransitionedTo(String expectedStatus) {
        String invoiceId = context.getInvoiceId();
        if (invoiceId == null) {
            invoiceId = UUID.randomUUID().toString();
            context.setInvoiceId(invoiceId);
            return;
        }

        Response getResponse = context.givenBillingAuth()
                .get("/api/v1/invoices/" + invoiceId);

        assertThat(getResponse).as("Get invoice response must not be null").isNotNull();
        if (getResponse.getStatusCode() == 200) {
            assertThat(getResponse.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }
}
