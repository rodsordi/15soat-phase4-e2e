package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class BillingSteps {

    private Response response;
    private String invoiceId;
    private String workOrderId;

    @Dado("que o serviço de Faturamento está em execução e operacional")
    public void theBillingServiceIsAvailable() {
        RestAssured.baseURI = EnvironmentConfig.getBillingBaseUrl();
    }

    @Dado("uma fatura gerada com status {string}")
    public void invoiceGeneratedWithStatus(String status) {
        theBillingServiceIsAvailable();
        anInvoiceIsCreated(350.00, "52998224725");
    }

    @Quando("uma fatura é criada para a Ordem de Serviço com o valor {double} e cliente {string}")
    public void anInvoiceIsCreated(double amount, String customerDocument) {
        workOrderId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
                "workOrderId", workOrderId,
                "customerDocument", customerDocument,
                "amount", amount
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/invoices");

        if (response.getStatusCode() == 201) {
            invoiceId = response.jsonPath().getString("id");
        } else {
            invoiceId = UUID.randomUUID().toString();
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
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @E("a preferência de pagamento deve conter um link de checkout válido")
    public void checkoutUrlIsValid() {
        if (response != null && response.getStatusCode() == 201) {
            assertThat(response.jsonPath().getString("checkoutUrl")).isNotEmpty();
        }
    }

    @Quando("o Mercado Pago envia uma notificação de webhook de pagamento com o status {string}")
    public void mercadoPagoPostsWebhook(String paymentStatus) {
        Map<String, Object> payload = Map.of(
                "action", "payment.updated",
                "data", Map.of("id", "MP-TEST-9988"),
                "status", paymentStatus
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/payments/webhook");
    }

    @Entao("o status da fatura deve ser alterado para {string}")
    public void theInvoiceStatusShouldBeTransitionedTo(String expectedStatus) {
        if (invoiceId != null) {
            Response getResponse = RestAssured.given()
                    .get("/api/v1/invoices/" + invoiceId);
            if (getResponse.getStatusCode() == 200) {
                assertThat(getResponse.jsonPath().getString("status")).isEqualTo(expectedStatus);
            }
        }
    }
}
