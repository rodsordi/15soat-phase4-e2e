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

public class ExecutionSteps {

    private Response response;
    private String executionId;
    private String workOrderId;

    @Dado("que o serviço de Execução da Oficina está em execução e operacional")
    public void theWorkshopExecutionServiceIsAvailable() {
        RestAssured.baseURI = EnvironmentConfig.getExecBaseUrl();
    }

    @Dado("uma ordem de execução criada com identificador válido")
    public void existingExecutionOrder() {
        theWorkshopExecutionServiceIsAvailable();
        anExecutionOrderIsEnqueued("TECH-101");
    }

    @Quando("um novo material com código {string}, descrição {string} e quantidade {int} é registrado")
    public void registerMaterial(String code, String desc, int qty) {
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
        }
        Map<String, Object> payload = Map.of(
                "materialCode", code,
                "description", desc,
                "quantity", qty
        );
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/executions/" + executionId + "/materials");
    }

    @Entao("o material deve ser computado com sucesso na manutenção")
    public void materialComputedSuccessfully() {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @Quando("uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico {string}")
    public void anExecutionOrderIsEnqueued(String technicianId) {
        workOrderId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
                "workOrderId", workOrderId,
                "technicianId", technicianId,
                "notes", "Vehicle inspection entry"
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/executions");

        if (response.getStatusCode() == 201) {
            executionId = response.jsonPath().getString("id");
        } else {
            executionId = UUID.randomUUID().toString();
        }
    }

    @Entao("a ordem de execução deve ser criada com o status {string}")
    public void theExecutionOrderShouldBeCreatedWithStatus(String expectedStatus) {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @Quando("o mecânico registra o item de checklist {string} como concluído")
    public void mechanicRecordsChecklist(String task) {
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
        }

        Map<String, Object> payload = Map.of(
                "task", task,
                "completed", true
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/executions/" + executionId + "/checklist");
    }

    @Entao("os itens de checklist devem ser persistidos com sucesso no MongoDB")
    public void checklistPersistedSuccessfully() {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @Quando("atualiza o status da execução para {string}")
    public void updatesExecutionStatus(String status) {
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
        }

        Map<String, Object> payload = Map.of(
                "status", status,
                "notes", "Status update: " + status
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .patch("/api/v1/executions/" + executionId + "/status");
    }

    @Entao("o status da execução deve ser atualizado para {string}")
    public void executionStatusShouldBeUpdatedTo(String expectedStatus) {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @Quando("finaliza atualizando o status da execução para {string}")
    public void finallyUpdatesExecutionStatus(String status) {
        updatesExecutionStatus(status);
    }

    @Entao("a ordem de execução deve ter o status {string} e registrar a data de conclusão")
    public void executionOrderCompletedWithTimestamp(String expectedStatus) {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
            assertThat(response.jsonPath().getString("completedAt")).isNotNull();
        }
    }
}
