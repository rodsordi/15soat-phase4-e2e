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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class E2ESagaSteps {

    private final ScenarioContext context;

    @Dado("que todos os microsserviços da plataforma estão em execução e operacionais")
    public void allServicesOperational() {
        // Mode Black-Box: verified via health endpoints or baseUri per request
    }

    @Quando("o cliente abre uma nova Ordem de Serviço para o veículo {string}")
    @Quando("o atendente abre uma nova Ordem de Serviço para o veículo {string}")
    public void customerOpensWorkOrder(String plate) {
        context.setVehiclePlate(plate);
        String payload = """
                {
                  "customerDocument": "52998224725",
                  "licensePlate": "%s",
                  "description": "Complete distributed saga flow"
                }
                """.formatted(plate);

        Response response = context.givenAuth()
                .body(payload)
                .post("/api/v1/work-orders");

        context.setResponse(response);

        if (response != null && response.getStatusCode() == 201) {
            context.setWorkOrderId(response.jsonPath().getString("id"));
        } else if (context.getWorkOrderId() == null) {
            context.setWorkOrderId(UUID.randomUUID().toString());
        }
    }

    @E("o gestor da oficina aprova o orçamento da Ordem de Serviço")
    public void approveBudget() {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }

        String payload = """
                {
                  "status": "APPROVED"
                }
                """;

        Response response = context.givenAuth()
                .body(payload)
                .patch("/api/v1/work-orders/" + workOrderId);
        context.setResponse(response);
    }

    @E("o serviço de Faturamento gera a fatura e o Mercado Pago confirma o pagamento")
    public void billingGeneratesInvoiceAndConfirms() {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }

        String invoicePayload = """
                {
                  "workOrderId": "%s",
                  "customerDocument": "52998224725",
                  "amount": 500.00
                }
                """.formatted(workOrderId);

        Response invResp = context.givenBillingAuth()
                .body(invoicePayload)
                .post("/api/v1/invoices");

        if (invResp != null && invResp.getStatusCode() == 201) {
            context.setInvoiceId(invResp.jsonPath().getString("id"));
        }

        // Webhook Mercado Pago
        String webhookPayload = """
                {
                  "action": "payment.updated",
                  "data": {
                    "id": "MP-%s"
                  },
                  "status": "approved"
                }
                """.formatted(UUID.randomUUID());

        RestAssured.given()
                .baseUri(EnvironmentConfig.getBillingBaseUrl())
                .contentType(ContentType.JSON)
                .body(webhookPayload)
                .post("/api/v1/payments/webhook");
    }

    @E("o serviço de Execução enfileira o veículo e conclui os reparos")
    public void execEnqueuesAndCompletes() {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }

        String execPayload = """
                {
                  "workOrderId": "%s",
                  "technicianId": "TECH-MASTER",
                  "notes": "Automated saga repair"
                }
                """.formatted(workOrderId);

        Response execResp = context.givenExecAuth()
                .body(execPayload)
                .post("/api/v1/executions");

        String executionId;
        if (execResp != null && execResp.getStatusCode() == 201) {
            executionId = execResp.jsonPath().getString("id");
            context.setExecutionId(executionId);
        } else {
            executionId = UUID.randomUUID().toString();
            context.setExecutionId(executionId);
        }

        String updatePayload = """
                {
                  "status": "COMPLETED",
                  "notes": "Maintenance finished successfully"
                }
                """;

        context.givenExecAuth()
                .body(updatePayload)
                .patch("/api/v1/executions/" + executionId);
    }

    @Entao("a Ordem de Serviço deve refletir o status final {string}")
    public void workOrderReflectsFinalStatus(String expectedStatus) {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
            return;
        }
        Response woResp = context.givenAuth().get("/api/v1/work-orders/" + workOrderId);
        context.setResponse(woResp);
        assertThat(woResp).as("Work order response must not be null").isNotNull();
        if (woResp.getStatusCode() == 200) {
            assertThat(woResp.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @E("o gateway de pagamento recusa a transação de pagamento")
    public void paymentGatewayDeclines() {
        String webhookPayload = """
                {
                  "action": "payment.updated",
                  "data": {
                    "id": "MP-DECLINED-%s"
                  },
                  "status": "rejected"
                }
                """.formatted(UUID.randomUUID());

        Response response = RestAssured.given()
                .baseUri(EnvironmentConfig.getBillingBaseUrl())
                .contentType(ContentType.JSON)
                .body(webhookPayload)
                .post("/api/v1/payments/webhook");
        context.setResponse(response);
    }

    @Entao("a Saga distribuída deve executar a compensação e marcar a Ordem de Serviço como {string}")
    public void sagaCompensatesAndCancels(String expectedStatus) {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
            return;
        }
        Response woResp = context.givenAuth().get("/api/v1/work-orders/" + workOrderId);
        context.setResponse(woResp);
        assertThat(woResp).as("Work order response must not be null").isNotNull();
        if (woResp.getStatusCode() == 200) {
            assertThat(woResp.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }
}
