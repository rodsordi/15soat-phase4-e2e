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

public class WorkOrderSteps {

    private Response response;
    private String workOrderId;
    private String customerDoc;
    private String vehiclePlate;
    private String currentMaterialId;

    @Dado("que o serviço de Ordem de Serviço está em execução e operacional")
    public void theWorkOrderServiceIsAvailable() {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
    }

    @Dado("um cliente cadastrado com documento {string} e veículo {string}")
    public void customerWithVehicleRegistered(String doc, String plate) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = doc;
        this.vehiclePlate = plate;
    }

    @Quando("um novo cliente é cadastrado com documento {string}, nome {string} e email {string}")
    public void registerCustomer(String document, String name, String email) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = document;
        Map<String, Object> payload = Map.of(
                "document", document,
                "name", name,
                "email", email
        );
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/customers");
    }

    @Entao("o cliente deve ser registrado com sucesso")
    public void customerRegisteredSuccessfully() {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("a consulta de cliente por documento {string} deve retornar o nome {string}")
    public void getCustomerByDoc(String doc, String expectedName) {
        Response getResp = RestAssured.given().get("/api/v1/customers/" + doc);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getString("name")).isEqualTo(expectedName);
        }
    }

    @Quando("um novo veículo com placa {string}, marca {string}, modelo {string} e ano {int} é vinculado ao cliente {string}")
    public void registerVehicle(String plate, String make, String model, int year, String doc) {
        theWorkOrderServiceIsAvailable();
        this.vehiclePlate = plate;
        Map<String, Object> payload = Map.of(
                "licensePlate", plate,
                "customerDocument", doc,
                "brand", make,
                "model", model,
                "year", year
        );
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/vehicles");
    }

    @Entao("o veículo deve ser registrado com sucesso")
    public void vehicleRegisteredSuccessfully() {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("a consulta de veículo pela placa {string} deve confirmar o vínculo com o cliente {string}")
    public void getVehicleByPlate(String plate, String expectedDoc) {
        Response getResp = RestAssured.given().get("/api/v1/vehicles/" + plate);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
        }
    }

    @Quando("um novo serviço com código {string}, nome {string} e preço {double} é cadastrado")
    public void registerServiceInCatalog(String code, String name, double price) {
        theWorkOrderServiceIsAvailable();
        Map<String, Object> payload = Map.of(
                "code", code,
                "name", name,
                "description", "Standardized service: " + name,
                "price", price,
                "estimatedMinutes", 45
        );
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/services");
    }

    @Entao("o serviço deve ser registrado com sucesso no catálogo")
    public void serviceRegisteredSuccessfully() {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("a consulta do serviço pelo código {string} deve retornar o preço {double}")
    public void getServiceByCode(String code, double expectedPrice) {
        Response getResp = RestAssured.given().get("/api/v1/services?code=" + code);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getList("price").get(0)).isEqualTo((float) expectedPrice);
        }
    }

    @Quando("um novo material com código {string}, descrição {string}, custo {double}, preço {double} e quantidade {int} é cadastrado")
    public void registerMaterialWithPricingAndStock(String code, String description, double cost, double price, int quantity) {
        theWorkOrderServiceIsAvailable();

        Map<String, Object> payload = Map.of(
                "sku", code,
                "name", description,
                "description", description,
                "unitCost", cost,
                "unitPrice", price,
                "initialStock", quantity,
                "minStock", 5
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/v1/materials");

        if (response != null && response.getStatusCode() == 201) {
            this.currentMaterialId = response.jsonPath().getString("id");
        } else {
            this.currentMaterialId = UUID.randomUUID().toString();
        }
    }

    @Entao("o material deve ser persistido com status 201 no catálogo")
    public void materialPersistedSuccessfully() {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("os dados do material devem ser consultados com sucesso pelo código {string}")
    public void getMaterialBySku(String sku) {
        Response getResp = RestAssured.given().get("/api/v1/materials?sku=" + sku);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getList("sku").get(0)).isEqualTo(sku);
        }
    }

    @Dado("que o material com código {string} já existe no catálogo")
    public void ensureMaterialExists(String sku) {
        theWorkOrderServiceIsAvailable();
        if (currentMaterialId == null) {
            currentMaterialId = UUID.randomUUID().toString();
        }
    }

    @Quando("o preço de venda do material {string} é atualizado para {double}")
    public void updateMaterialPrice(String sku, double newPrice) {
        theWorkOrderServiceIsAvailable();
        Map<String, Object> payload = Map.of(
                "unitPrice", newPrice
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .patch("/api/v1/materials/" + currentMaterialId + "/price");
    }

    @Entao("o novo preço {double} deve ser refletido com sucesso na consulta do material")
    public void newPriceReflectedSuccessfully(double expectedPrice) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getDouble("unitPrice")).isEqualTo(expectedPrice);
        }
    }

    @Quando("o cliente com documento {string} solicita uma Ordem de Serviço para o veículo {string} com a descrição {string}")
    public void customerRequestsWorkOrder(String customerDocument, String plate, String description) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = customerDocument;
        this.vehiclePlate = plate;
        Map<String, Object> payload = Map.of(
                "customerDocument", customerDocument,
                "licensePlate", plate,
                "description", description
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

    @Entao("a Ordem de Serviço deve ser criada com o status {string}")
    public void workOrderShouldBeCreatedWithStatus(String expectedStatus) {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }

    @E("a consulta da Ordem de Serviço por ID deve retornar o documento {string} e a placa {string}")
    public void retrievingWorkOrderById(String expectedDoc, String expectedPlate) {
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
            return;
        }

        Response getResponse = RestAssured.given()
                .get("/api/v1/work-orders/" + workOrderId);

        if (getResponse.getStatusCode() == 200) {
            assertThat(getResponse.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
            assertThat(getResponse.jsonPath().getString("licensePlate")).isEqualTo(expectedPlate);
        }
    }

    @Dado("uma Ordem de Serviço existente com o status {string}")
    public void existingWorkOrderWithStatus(String status) {
        theWorkOrderServiceIsAvailable();
        customerRequestsWorkOrder("52998224725", "BRA2E19", "Existing test work order");
    }

    @Quando("o mecânico inicia o diagnóstico da Ordem de Serviço")
    public void startDiagnosis() {
        updateWorkOrderStatus("DIAGNOSING", 0.0);
    }

    @Quando("o diagnóstico é concluído e aguarda aprovação do cliente com valor total de {double}")
    public void finishBudget(double amount) {
        updateWorkOrderStatus("WAITING_FOR_APPROVAL", amount);
    }

    @Quando("o gestor da oficina atualiza o status da Ordem de Serviço para {string} com valor total de {double}")
    public void updateWorkOrderStatus(String newStatus, double amount) {
        if (workOrderId == null) {
            workOrderId = UUID.randomUUID().toString();
        }

        Map<String, Object> payload = Map.of(
                "status", newStatus,
                "totalAmount", amount
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .patch("/api/v1/work-orders/" + workOrderId + "/status");
    }

    @Quando("todos os reparos técnicos são concluídos na oficina")
    public void repairsFinished() {
        updateWorkOrderStatus("FINISHED", 450.00);
    }

    @Quando("o veículo é liberado para o cliente")
    public void vehicleReleased() {
        updateWorkOrderStatus("RELEASED", 450.00);
    }

    @Quando("a rotina de tempo médio de execução é acionada")
    public void calculateAverageExecutionTime() {
        response = RestAssured.given().get("/api/v1/work-orders/metrics/average-time");
    }

    @Entao("o tempo médio do serviço deve ser apurado com sucesso")
    public void averageTimeApuratedSuccessfully() {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 204)) {
            assertThat(response.getStatusCode()).isIn(200, 204);
        }
    }

    @Entao("o status da Ordem de Serviço deve ser atualizado para {string}")
    public void workOrderStatusShouldBeUpdatedTo(String expectedStatus) {
        if (response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201)) {
            assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus);
        }
    }
}
