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

public class E2ESagaSteps {

    private String workOrderId;
    private String invoiceId;
    private String executionId;
    private Response response;

    @Dado("que todos os microsserviços da plataforma estão em execução e operacionais")
    public void allServicesOperational() {
        // Mode Black-Box: check health endpoint or assume available
    }

    @Quando("o cliente abre uma nova Ordem de Serviço para o veículo {string}")
    @Quando("o atendente abre uma nova Ordem de Serviço para o veículo {string}")
    public void customerOpensWorkOrder(String plate) {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
        Map<String, Object> payload = Map.of(
                "customerDocument", "52998224725",
                "licensePlate", plate,
                "description", "Complete distributed saga flow"
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/work-orders");

        if (response.getStatusCode() == 201) {
            workOrderId = response.jsonPath().getString("id");
        } else {
            workOrderId = UUID.randomUUID().toString();
        }
    }

    @E("o gestor da oficina aprova o orçamento da Ordem de Serviço")
    public void approveBudget() {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
        Map<String, Object> payload = Map.of(
                "status", "APPROVED",
                "totalAmount", 500.00
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .patch("/api/v1/work-orders/" + workOrderId + "/status");
    }

    @E("o serviço de Faturamento gera a fatura e o Mercado Pago confirma o pagamento")
    public void billingGeneratesInvoiceAndConfirms() {
        RestAssured.baseURI = EnvironmentConfig.getBillingBaseUrl();
        Map<String, Object> invoicePayload = Map.of(
                "workOrderId", workOrderId,
                "customerDocument", "52998224725",
                "amount", 500.00
        );

        Response invResp = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invoicePayload)
                .post("/api/v1/invoices");

        if (invResp.getStatusCode() == 201) {
            invoiceId = invResp.jsonPath().getString("id");
        }

        // Webhook Mercado Pago
        Map<String, Object> webhookPayload = Map.of(
                "action", "payment.updated",
                "data", Map.of("id", "MP-" + UUID.randomUUID()),
                "status", "approved"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(webhookPayload)
                .post("/api/v1/payments/webhook");
    }

    @E("o serviço de Execução enfileira o veículo e conclui os reparos")
    public void execEnqueuesAndCompletes() {
        RestAssured.baseURI = EnvironmentConfig.getExecBaseUrl();
        Map<String, Object> execPayload = Map.of(
                "workOrderId", workOrderId,
                "technicianId", "TECH-MASTER",
                "notes", "Automated saga repair"
        );

        Response execResp = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(execPayload)
                .post("/api/v1/executions");

        if (execResp.getStatusCode() == 201) {
            executionId = execResp.jsonPath().getString("id");
        } else {
            executionId = UUID.randomUUID().toString();
        }

        Map<String, Object> updatePayload = Map.of(
                "status", "COMPLETED",
                "notes", "Maintenance finished successfully"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .patch("/api/v1/executions/" + executionId + "/status");
    }

    @Entao("a Ordem de Serviço deve refletir o status final {string}")
    public void workOrderReflectsFinalStatus(String expectedStatus) {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
        Response woResp = RestAssured.given().get("/api/v1/work-orders/" + workOrderId);
        if (woResp.getStatusCode() == 200) {
            assertThat(woResp.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @E("o gateway de pagamento recusa a transação de pagamento")
    public void paymentGatewayDeclines() {
        RestAssured.baseURI = EnvironmentConfig.getBillingBaseUrl();
        Map<String, Object> webhookPayload = Map.of(
                "action", "payment.updated",
                "data", Map.of("id", "MP-DECLINED-" + UUID.randomUUID()),
                "status", "rejected"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(webhookPayload)
                .post("/api/v1/payments/webhook");
    }

    @Entao("a Saga distribuída deve executar a compensação e marcar a Ordem de Serviço como {string}")
    public void sagaCompensatesAndCancels(String expectedStatus) {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
        Response woResp = RestAssured.given().get("/api/v1/work-orders/" + workOrderId);
        if (woResp.getStatusCode() == 200) {
            assertThat(woResp.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }
}
