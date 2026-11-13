package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ExecutionSteps {

    private final ScenarioContext context;

    @Dado("que o serviço de Execução da Oficina está em execução e operacional")
    public void theWorkshopExecutionServiceIsAvailable() {
        // Operational check verified via baseUri per request
    }

    @Dado("que o mecânico responsável está devidamente autenticado com perfil {string}")
    public void mechanicIsAuthenticated(String role) {
        context.setCurrentAuthToken("Bearer valid_" + role.toLowerCase() + "_token");
    }

    @Dado("uma ordem de execução criada com identificador válido")
    public void existingExecutionOrder() {
        anExecutionOrderIsEnqueued("TECH-101");
    }

    @Quando("um novo material com código {string}, descrição {string} e quantidade {int} é registrado")
    public void registerMaterial(String code, String desc, int qty) {
        String executionId = context.getExecutionId();
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
            context.setExecutionId(executionId);
        }
        String payload = """
                {
                  "materialCode": "%s",
                  "description": "%s",
                  "quantity": %d
                }
                """.formatted(code, desc, qty);

        Response response = context.givenExecAuth()
                .body(payload)
                .post("/api/v1/executions/" + executionId + "/materials");
        context.setResponse(response);
    }

    @Quando("o material com os seguintes dados é registrado na manutenção:")
    public void registerMaterialFromTable(Map<String, String> data) {
        String code = data.get("materialCode");
        String desc = data.get("description");
        int qty = Integer.parseInt(data.get("quantity"));
        registerMaterial(code, desc, qty);
    }

    @Entao("o material deve ser computado com sucesso na manutenção")
    public void materialComputedSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("Execution response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @Quando("uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico {string}")
    public void anExecutionOrderIsEnqueued(String technicianId) {
        context.setTechnicianId(technicianId);
        String workOrderId = context.getWorkOrderId();
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            context.setWorkOrderId(workOrderId);
        }
        String payload = """
                {
                  "workOrderId": "%s",
                  "technicianId": "%s",
                  "notes": "Vehicle inspection entry"
                }
                """.formatted(workOrderId, technicianId);

        Response response = context.givenExecAuth()
                .body(payload)
                .post("/api/v1/executions");

        context.setResponse(response);

        if (response != null && response.getStatusCode() == 201) {
            context.setExecutionId(response.jsonPath().getString("id"));
        } else {
            context.setExecutionId(UUID.randomUUID().toString());
        }
    }

    @Entao("a ordem de execução deve ser criada com o status {string}")
    public void theExecutionOrderShouldBeCreatedWithStatus(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Execution response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
    }

    @Quando("o mecânico registra o item de checklist {string} como concluído")
    public void mechanicRecordsChecklist(String task) {
        String executionId = context.getExecutionId();
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
            context.setExecutionId(executionId);
        }

        String payload = """
                {
                  "task": "%s",
                  "completed": true
                }
                """.formatted(task);

        Response response = context.givenExecAuth()
                .body(payload)
                .post("/api/v1/executions/" + executionId + "/checklist");
        context.setResponse(response);
    }

    @Quando("o mecânico registra os seguintes itens de checklist:")
    public void mechanicRecordsChecklistItems(io.cucumber.datatable.DataTable dataTable) {
        java.util.List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            mechanicRecordsChecklist(row.get("task"));
        }
    }

    @Entao("os itens de checklist devem ser persistidos com sucesso no MongoDB")
    public void checklistPersistedSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("Execution response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @Quando("atualiza o status da execução para {string}")
    @Quando("o mecânico inicia a execução do reparo com status {string}")
    public void updatesExecutionStatus(String status) {
        String executionId = context.getExecutionId();
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
            context.setExecutionId(executionId);
        }

        String payload = """
                {
                  "status": "%s",
                  "notes": "Status update: %s"
                }
                """.formatted(status, status);

        Response response = context.givenExecAuth()
                .body(payload)
                .patch("/api/v1/executions/" + executionId);
        context.setResponse(response);
    }

    @Entao("o status da execução deve ser atualizado para {string}")
    public void executionStatusShouldBeUpdatedTo(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Execution response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
    }

    @Quando("finaliza atualizando o status da execução para {string}")
    @Quando("o mecânico conclui a execução do reparo com status {string}")
    public void finallyUpdatesExecutionStatus(String status) {
        updatesExecutionStatus(status);
    }

    @Entao("a ordem de execução deve ter o status {string} e registrar a data de conclusão")
    public void executionOrderCompletedWithTimestamp(String expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Execution response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getString("completedAt")).isNotNull();
    }

    @Quando("o supervisor consulta a fila de execução da oficina")
    public void supervisorQueriesExecutionQueue() {
        Response response = context.givenExecAuth()
                .get("/api/v1/executions/queue");
        context.setResponse(response);
    }

    @Entao("a fila deve listar a ordem de execução com paginação e status ativo")
    public void queueShouldListExecutionOrder() {
        Response response = context.getResponse();
        assertThat(response).as("Execution queue response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("content")).isNotEmpty();
        assertThat(response.jsonPath().getInt("totalElements")).isGreaterThanOrEqualTo(1);
    }

    @Quando("o mecânico registra uma interrupção técnica com status {string} e justificativa {string}")
    public void mechanicRecordsTechnicalFailure(String status, String notes) {
        String executionId = context.getExecutionId();
        assertThat(executionId).as("Execution ID must not be null").isNotNull();
        String payload = """
                {
                  "status": "%s",
                  "notes": "%s"
                }
                """.formatted(status, notes);

        Response response = context.givenExecAuth()
                .body(payload)
                .patch("/api/v1/executions/" + executionId);
        context.setResponse(response);
    }

    @Quando("o mecânico tenta atualizar o status da execução para {string}")
    public void mechanicAttemptsToUpdateStatus(String status) {
        updatesExecutionStatus(status);
    }

    @Entao("o sistema deve rejeitar a operação informando violação de regra de negócio com código {int}")
    public void systemRejectsWithBusinessRuleViolation(int expectedStatusCode) {
        Response response = context.getResponse();
        assertThat(response).as("Response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatusCode);
    }

    @Quando("o mecânico consulta os detalhes consolidados da execução por ID")
    public void mechanicQueriesConsolidatedExecutionDetails() {
        String executionId = context.getExecutionId();
        assertThat(executionId).as("Execution ID must not be null").isNotNull();
        Response response = context.givenExecAuth()
                .get("/api/v1/executions/" + executionId);
        context.setResponse(response);
    }

    @Entao("os dados consolidados devem conter os materiais e itens de checklist registrados")
    public void consolidatedDataShouldContainMaterialsAndChecklist() {
        Response response = context.getResponse();
        assertThat(response).as("Consolidated execution details response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("materials")).isNotEmpty();
        assertThat(response.jsonPath().getList("checklist")).isNotEmpty();
    }
}
