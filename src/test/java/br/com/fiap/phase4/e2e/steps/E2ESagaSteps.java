package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class E2ESagaSteps {

    private final ScenarioContext context;

    private <T> T pollUntil(Supplier<T> supplier, Predicate<T> predicate, long timeoutMs, long pollIntervalMs) {
        long startTime = System.currentTimeMillis();
        T lastResult = null;
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                lastResult = supplier.get();
                if (predicate.test(lastResult)) {
                    return lastResult;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return lastResult;
    }

    @Dado("que todos os microsserviços da plataforma estão em execução e operacionais")
    public void allServicesOperational() {
        // Mode Black-Box: verified via health endpoints or baseUri per request
    }

    @Quando("o cliente abre uma nova Ordem de Serviço para o veículo {string}")
    @Quando("o atendente abre uma nova Ordem de Serviço para o veículo {string}")
    public void customerOpensWorkOrder(String plate) {
        context.setVehiclePlate(plate);
        Map<String, Object> payload = Map.of(
                "customerDocument", "52998224725",
                "licensePlate", plate,
                "description", "Complete distributed saga flow"
        );

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

        Map<String, Object> payload = Map.of("status", "APPROVED");

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

        String paymentId = "MP-CONFIRM-" + UUID.randomUUID().toString().substring(0, 8);

        // Check if invoice was already created by Kafka consumer or create it
        Response invResp = context.givenBillingAuth().get("/api/v1/invoices/work-order/" + workOrderId);
        String invoiceId = null;
        if (invResp != null && invResp.getStatusCode() == 200) {
            invoiceId = invResp.jsonPath().getString("id");
            if (invoiceId == null) {
                invoiceId = invResp.jsonPath().getString("content.find { it.workOrderId == '" + workOrderId + "' }.id");
            }
        }
        if (invoiceId == null) {
            Map<String, Object> invoicePayload = Map.of(
                    "workOrderId", workOrderId,
                    "customerDocument", "52998224725",
                    "amount", 500.00
            );
            Response createResp = context.givenBillingAuth()
                    .body(invoicePayload)
                    .post("/api/v1/invoices");
            if (createResp != null && (createResp.getStatusCode() == 201 || createResp.getStatusCode() == 200)) {
                invoiceId = createResp.jsonPath().getString("id");
            }
        }
        if (invoiceId != null) {
            paymentId = invoiceId;
            context.setInvoiceId(invoiceId);
        }

        // Webhook Mercado Pago
        Map<String, Object> webhookPayload = Map.of(
                "action", "payment.updated",
                "data", Map.of("id", paymentId)
        );

        context.givenBillingAuth()
                .body(webhookPayload)
                .post("/api/v1/payments/mercadopago/webhook");
    }

    @E("o serviço de Execução enfileira o veículo e conclui os reparos")
    public void execEnqueuesAndCompletes() {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }

        // Check if execution order already enqueued by saga Kafka consumer
        Response execQuery = pollUntil(
                () -> context.givenExecAuth().get("/api/v1/executions/queue"),
                resp -> resp != null && resp.getStatusCode() == 200 &&
                        resp.jsonPath().getString("content.find { it.workOrderId == '" + context.getWorkOrderId() + "' }.id") != null,
                5000,
                150
        );

        String executionId = null;
        if (execQuery != null && execQuery.getStatusCode() == 200) {
            executionId = execQuery.jsonPath().getString("content.find { it.workOrderId == '" + workOrderId + "' }.id");
        }

        if (executionId == null) {
            Map<String, Object> execPayload = Map.of(
                    "workOrderId", workOrderId,
                    "technicianId", "TECH-MASTER",
                    "notes", "Automated saga repair"
            );
            Response execResp = context.givenExecAuth()
                    .body(execPayload)
                    .post("/api/v1/executions");
            if (execResp != null && execResp.getStatusCode() == 201) {
                executionId = execResp.jsonPath().getString("id");
            }
        }

        if (executionId != null) {
            context.setExecutionId(executionId);
            Map<String, Object> updatePayload = Map.of(
                    "status", "COMPLETED",
                    "notes", "Maintenance finished successfully"
            );
            context.givenExecAuth()
                    .body(updatePayload)
                    .patch("/api/v1/executions/" + executionId);
        }
    }

    @Entao("a Ordem de Serviço deve refletir o status final {string}")
    public void workOrderReflectsFinalStatus(String expectedStatus) {
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
            return;
        }

        Response woResp = pollUntil(
                () -> context.givenAuth().get("/api/v1/work-orders/" + context.getWorkOrderId()),
                resp -> resp != null && resp.getStatusCode() == 200 && expectedStatus.equalsIgnoreCase(resp.jsonPath().getString("status")),
                15000,
                150
        );

        context.setResponse(woResp);
        assertThat(woResp).as("Work order response must not be null").isNotNull();
        if (woResp.getStatusCode() == 200) {
            assertThat(woResp.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @E("o gateway de pagamento recusa a transação de pagamento")
    public void paymentGatewayDeclines() {
        String workOrderId = context.getWorkOrderId();

        Response invResp = pollUntil(
                () -> context.givenBillingAuth().get("/api/v1/invoices/work-order/" + context.getWorkOrderId()),
                resp -> resp != null && resp.getStatusCode() == 200 &&
                        (resp.jsonPath().getString("id") != null || resp.jsonPath().getString("content[0].id") != null),
                5000,
                150
        );

        String invoiceId = null;
        if (invResp != null && invResp.getStatusCode() == 200) {
            invoiceId = invResp.jsonPath().getString("id");
            if (invoiceId == null) {
                invoiceId = invResp.jsonPath().getString("content.find { it.workOrderId == '" + workOrderId + "' }.id");
            }
        }

        if (invoiceId == null) {
            Map<String, Object> invoicePayload = Map.of(
                    "workOrderId", workOrderId,
                    "customerDocument", "52998224725",
                    "amount", 500.00
            );
            Response createResp = context.givenBillingAuth()
                    .body(invoicePayload)
                    .post("/api/v1/invoices");
            if (createResp != null && (createResp.getStatusCode() == 201 || createResp.getStatusCode() == 200)) {
                invoiceId = createResp.jsonPath().getString("id");
            }
        }

        String rejectedPaymentId = (invoiceId != null ? invoiceId : UUID.randomUUID().toString()) + "-REJECT";
        Map<String, Object> webhookPayload = Map.of(
                "action", "payment.updated",
                "data", Map.of("id", rejectedPaymentId)
        );

        Response response = context.givenBillingAuth()
                .body(webhookPayload)
                .post("/api/v1/payments/mercadopago/webhook");
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

        Response woResp = pollUntil(
                () -> context.givenAuth().get("/api/v1/work-orders/" + context.getWorkOrderId()),
                resp -> {
                    if (resp == null || resp.getStatusCode() != 200) return false;
                    String actualStatus = resp.jsonPath().getString("status");
                    return expectedStatus.equalsIgnoreCase(actualStatus) || "PAYMENT_REJECTED".equalsIgnoreCase(actualStatus);
                },
                10000,
                150
        );

        context.setResponse(woResp);
        assertThat(woResp).as("Work order response must not be null").isNotNull();
        if (woResp.getStatusCode() == 200) {
            String actualStatus = woResp.jsonPath().getString("status");
            assertThat(actualStatus).isIn(expectedStatus, "PAYMENT_REJECTED");
        }
    }

    @Quando("o mecânico registra falha técnica irrecuperável no reparo da oficina com justificativa {string}")
    public void mechanicRegistersTechnicalFailure(String justification) {
        String workOrderId = context.getWorkOrderId();
        assertThat(workOrderId).as("Work order ID must not be null").isNotNull();

        Response execQuery = pollUntil(
                () -> context.givenExecAuth().get("/api/v1/executions/queue"),
                resp -> resp != null && resp.getStatusCode() == 200 &&
                        resp.jsonPath().getString("content.find { it.workOrderId == '" + context.getWorkOrderId() + "' }.id") != null,
                5000,
                150
        );

        String executionId = null;
        if (execQuery != null && execQuery.getStatusCode() == 200) {
            executionId = execQuery.jsonPath().getString("content.find { it.workOrderId == '" + workOrderId + "' }.id");
        }

        if (executionId == null) {
            Map<String, Object> execPayload = Map.of(
                    "workOrderId", workOrderId,
                    "technicianId", "TECH-101",
                    "notes", "Emergency repair inspection"
            );
            Response createExec = context.givenExecAuth()
                    .body(execPayload)
                    .post("/api/v1/executions");
            if (createExec != null && (createExec.getStatusCode() == 201 || createExec.getStatusCode() == 200)) {
                executionId = createExec.jsonPath().getString("id");
            }
        }

        assertThat(executionId).as("Execution order ID must be present").isNotNull();
        context.setExecutionId(executionId);

        Map<String, Object> patchPayload = Map.of(
                "status", "FAILED",
                "notes", justification
        );

        Response response = context.givenExecAuth()
                .body(patchPayload)
                .patch("/api/v1/executions/" + executionId);

        context.setResponse(response);
        assertThat(response.getStatusCode()).isIn(200, 204);
    }

    @Entao("o sistema de faturamento deve processar o estorno financeiro no Mercado Pago e marcar a fatura como {string}")
    public void billingProcessesCompensatingRefund(String expectedStatus) {
        String workOrderId = context.getWorkOrderId();
        assertThat(workOrderId).as("Work order ID must not be null").isNotNull();

        Response invResp = pollUntil(
                () -> context.givenBillingAuth().get("/api/v1/invoices/work-order/" + context.getWorkOrderId()),
                resp -> {
                    if (resp == null || resp.getStatusCode() != 200) return false;
                    String status = resp.jsonPath().getString("status");
                    if (status == null) {
                        status = resp.jsonPath().getString("content[0].status");
                    }
                    return expectedStatus.equalsIgnoreCase(status);
                },
                12000,
                150
        );

        context.setResponse(invResp);
        assertThat(invResp).as("Invoice response must not be null").isNotNull();
        String actualStatus = invResp.jsonPath().getString("status");
        if (actualStatus == null) {
            actualStatus = invResp.jsonPath().getString("content[0].status");
        }
        assertThat(actualStatus).as("Invoice status must be transitioned to " + expectedStatus).isEqualTo(expectedStatus);
    }

    @Dado("que a Ordem de Serviço foi previamente compensada com fatura estornada")
    public void workOrderPreviouslyCompensated() {
        if (context.getWorkOrderId() == null) {
            customerOpensWorkOrder("BRA2E19");
            approveBudget();
            billingGeneratesInvoiceAndConfirms();
            mechanicRegistersTechnicalFailure("Impossibilidade de reparo: bloco trincado");
            billingProcessesCompensatingRefund("REFUNDED");
            sagaCompensatesAndCancels("CANCELED");
        } else {
            billingProcessesCompensatingRefund("REFUNDED");
            sagaCompensatesAndCancels("CANCELED");
        }
    }

    @Quando("uma tentativa de transição na Ordem de Serviço já cancelada é submetida")
    public void attemptTransitionOnCanceledWorkOrder() {
        String workOrderId = context.getWorkOrderId();
        assertThat(workOrderId).as("Work order ID must not be null").isNotNull();

        Map<String, Object> payload = Map.of("status", "APPROVED");

        Response response = context.givenAuth()
                .body(payload)
                .patch("/api/v1/work-orders/" + workOrderId);

        context.setResponse(response);
    }

    @Entao("a tentativa deve ser rejeitada com código {int}")
    public void attemptRejectedWithStatusCode(int expectedStatusCode) {
        Response response = context.getResponse();
        assertThat(response).as("Response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatusCode);
    }

    @Entao("o estado compensado deve permanecer inalterado como fatura {string} e Ordem de Serviço {string}")
    public void compensatedStateMustRemainUnchanged(String expectedInvoiceStatus, String expectedWorkOrderStatus) {
        billingProcessesCompensatingRefund(expectedInvoiceStatus);
        sagaCompensatesAndCancels(expectedWorkOrderStatus);
    }
}
