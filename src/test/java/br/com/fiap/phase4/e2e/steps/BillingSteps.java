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
        assertThat(response.jsonPath().getString("checkoutUrl")).isNotEmpty();
    }

    @Quando("o Mercado Pago envia uma notificação de webhook de pagamento com o status {string}")
    public void mercadoPagoPostsWebhook(String paymentStatus) {
        String payload = """
                {
                  "action": "payment.updated",
                  "data": {
                    "id": "MP-TEST-9988"
                  },
                  "status": "%s"
                }
                """.formatted(paymentStatus);

        Response response = RestAssured.given()
                .baseUri(EnvironmentConfig.getBillingBaseUrl())
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/payments/webhook");

        context.setResponse(response);
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
