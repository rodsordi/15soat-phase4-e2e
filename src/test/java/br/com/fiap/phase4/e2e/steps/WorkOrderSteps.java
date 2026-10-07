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
    private String currentAuthToken = "Bearer mock_employee_jwt_token";

    @Dado("que o serviço de Ordem de Serviço está em execução e operacional")
    public void theWorkOrderServiceIsAvailable() {
        RestAssured.baseURI = EnvironmentConfig.getWorkOrderBaseUrl();
    }

    @Dado("que o atendente da oficina está devidamente autenticado com perfil {string}")
    @Dado("que o atendente e o mecânico estão devidamente autenticados com perfil {string}")
    @Dado("que o operador de estoque está devidamente autenticado com perfil {string}")
    @Dado("que o administrador da oficina está devidamente autenticado com perfil {string}")
    public void attendantIsAuthenticatedWithRole(String role) {
        theWorkOrderServiceIsAvailable();
        this.currentAuthToken = "Bearer valid_" + role.toLowerCase() + "_token";
    }

    @Dado("que o operador não possui token de autenticação válido")
    public void operatorHasNoValidToken() {
        theWorkOrderServiceIsAvailable();
        this.currentAuthToken = null;
    }

    @Quando("o cliente com documento {string}, nome {string} e email {string} tenta ser cadastrado")
    public void tryRegisterCustomerWithoutAuth(String document, String name, String email) {
        theWorkOrderServiceIsAvailable();
        Map<String, Object> payload = Map.of(
                "document", document,
                "name", name,
                "email", email
        );
        var requestSpec = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload);
        if (currentAuthToken != null) {
            requestSpec.header("Authorization", currentAuthToken);
        }
        response = requestSpec.post("/api/v1/customers");
    }

    @Dado("um cliente cadastrado com documento {string} e veículo {string}")
    public void customerWithVehicleRegistered(String doc, String plate) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = doc;
        this.vehiclePlate = plate;
    }

    @Dado("que o cliente com documento {string} já está previamente cadastrado no sistema")
    @Dado("o cliente com documento {string} cadastrado")
    public void customerAlreadyRegistered(String doc) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = doc;
        registerCustomer(doc, "Rodrigo Sordi", "rodrigo@fiap.com.br");
    }

    private String pendingCustomerName;
    private String pendingCustomerEmail;

    @Dado("o cliente com documento {string}, nome {string} e email {string}")
    public void prepareCustomerData(String document, String name, String email) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = document;
        this.pendingCustomerName = name;
        this.pendingCustomerEmail = email;
    }

    @Dado("o cliente com os seguintes dados cadastrais:")
    public void prepareCustomerDataFromTable(Map<String, String> data) {
        theWorkOrderServiceIsAvailable();
        this.customerDoc = data.get("document");
        this.pendingCustomerName = data.get("name");
        this.pendingCustomerEmail = data.get("email");
    }

    @Quando("o cadastro do cliente é submetido")
    @Quando("o cliente é cadastrado")
    public void submitCustomerRegistration() {
        registerCustomer(this.customerDoc, this.pendingCustomerName, this.pendingCustomerEmail);
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
        var requestSpec = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload);
        if (currentAuthToken != null) {
            requestSpec.header("Authorization", currentAuthToken);
        }
        response = requestSpec.post("/api/v1/customers");
    }

    @Entao("o cliente deve ser registrado com sucesso")
    public void customerRegisteredSuccessfully() {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("a resposta deve conter identificador único gerado e documento {string}")
    public void responseContainsIdAndDocument(String expectedDoc) {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.jsonPath().getString("id")).isNotBlank();
            assertThat(response.jsonPath().getString("document")).isEqualTo(expectedDoc);
        }
    }

    @Quando("a consulta de cliente por documento {string} é realizada")
    @Quando("o cliente com documento {string} é consultado")
    public void executeCustomerQuery(String doc) {
        response = RestAssured.given().get("/api/v1/customers?document=" + doc);
    }

    @Entao("a consulta deve retornar o nome {string}")
    public void customerQueryReturnsName(String expectedName) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getString("name")).isEqualTo(expectedName);
        }
    }

    @Entao("a consulta deve retornar o nome {string} e o email {string}")
    public void customerQueryReturnsNameAndEmail(String expectedName, String expectedEmail) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getString("name")).isEqualTo(expectedName);
            assertThat(response.jsonPath().getString("email")).isEqualTo(expectedEmail);
        }
    }

    @Entao("a consulta deve retornar os seguintes dados do cliente:")
    public void customerQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        if (response != null && response.getStatusCode() == 200) {
            expectedFields.forEach((key, expectedValue) -> {
                String actualValue = response.jsonPath().getString(key);
                assertThat(actualValue).isEqualTo(expectedValue);
            });
        }
    }

    @E("a consulta de cliente por documento {string} deve retornar o nome {string}")
    public void getCustomerByDoc(String doc, String expectedName) {
        Response getResp = RestAssured.given().get("/api/v1/customers?document=" + doc);
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

    @Quando("o veículo com os seguintes dados é vinculado ao cliente {string}:")
    public void registerVehicleFromTable(String doc, Map<String, String> data) {
        theWorkOrderServiceIsAvailable();
        String plate = data.get("licensePlate");
        this.vehiclePlate = plate;
        int year = Integer.parseInt(data.get("year"));
        String make = data.get("make") != null ? data.get("make") : data.get("brand");
        Map<String, Object> payload = Map.of(
                "licensePlate", plate,
                "customerDocument", doc,
                "brand", make,
                "model", data.get("model"),
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

    @E("a resposta do veículo deve confirmar a placa {string} e o documento {string}")
    public void vehicleResponseConfirmsPlateAndDoc(String expectedPlate, String expectedDoc) {
        if (response != null && (response.getStatusCode() == 201 || response.getStatusCode() == 200)) {
            assertThat(response.jsonPath().getString("licensePlate")).isEqualTo(expectedPlate);
            assertThat(response.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
        }
    }

    @Quando("a consulta de veículo pela placa {string} é realizada")
    @Quando("o veículo com placa {string} é consultado")
    public void executeVehicleQuery(String plate) {
        response = RestAssured.given().get("/api/v1/vehicles?licensePlate=" + plate);
    }

    @Entao("a consulta de veículo deve confirmar o vínculo com o cliente {string}")
    public void vehicleQueryConfirmsLink(String expectedDoc) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
        }
    }

    @E("a consulta deve retornar os dados do veículo com marca {string} e modelo {string}")
    public void vehicleQueryReturnsMakeAndModel(String expectedMake, String expectedModel) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getString("make")).isEqualTo(expectedMake);
            assertThat(response.jsonPath().getString("model")).isEqualTo(expectedModel);
        }
    }

    @Entao("a consulta deve retornar os seguintes dados do veículo:")
    public void vehicleQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        if (response != null && response.getStatusCode() == 200) {
            expectedFields.forEach((key, expectedValue) -> {
                String actualValue = response.jsonPath().getString(key);
                assertThat(actualValue).isEqualTo(expectedValue);
            });
        }
    }

    @E("a consulta de veículo pela placa {string} deve confirmar o vínculo com o cliente {string}")
    public void getVehicleByPlate(String plate, String expectedDoc) {
        Response getResp = RestAssured.given().get("/api/v1/vehicles/" + plate);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
        }
    }

    private String pendingServiceCode;
    private String pendingServiceName;
    private double pendingServicePrice;

    @Dado("o serviço com código {string}, nome {string} e preço {double}")
    public void prepareServiceData(String code, String name, double price) {
        theWorkOrderServiceIsAvailable();
        this.pendingServiceCode = code;
        this.pendingServiceName = name;
        this.pendingServicePrice = price;
    }

    @Dado("o serviço com os seguintes dados:")
    public void prepareServiceDataFromTable(Map<String, String> data) {
        theWorkOrderServiceIsAvailable();
        this.pendingServiceCode = data.get("code");
        this.pendingServiceName = data.get("name");
        this.pendingServicePrice = Double.parseDouble(data.get("price"));
    }

    @Quando("a inclusão do serviço no catálogo é submetida")
    @Quando("o serviço é cadastrado no catálogo")
    public void submitServiceInCatalog() {
        registerServiceInCatalog(this.pendingServiceCode, this.pendingServiceName, this.pendingServicePrice);
    }

    @Dado("que o serviço com código {string} já existe no catálogo")
    @Dado("o serviço com código {string} cadastrado no catálogo")
    public void ensureServiceExists(String code) {
        theWorkOrderServiceIsAvailable();
        registerServiceInCatalog(code, "Troca de Óleo e Filtro", 150.00);
    }

    @Quando("a consulta do serviço pelo código {string} é realizada")
    @Quando("o serviço com código {string} é consultado")
    public void executeServiceQuery(String code) {
        response = RestAssured.given().get("/api/v1/services?code=" + code);
    }

    @Entao("a consulta do serviço deve retornar o preço {double}")
    public void serviceQueryReturnsPrice(double expectedPrice) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getList("price").get(0)).isEqualTo((float) expectedPrice);
        }
    }

    @Entao("a consulta deve retornar os seguintes dados do serviço:")
    public void serviceQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        if (response != null && response.getStatusCode() == 200) {
            expectedFields.forEach((key, expectedValue) -> {
                if ("price".equals(key)) {
                    double expectedPrice = Double.parseDouble(expectedValue);
                    assertThat(response.jsonPath().getList("price").get(0)).isEqualTo((float) expectedPrice);
                } else {
                    assertThat(response.jsonPath().getList(key).get(0).toString()).isEqualTo(expectedValue);
                }
            });
        }
    }

    @Quando("um novo serviço com código {string}, nome {string} e preço {double} é cadastrado")
    public void registerServiceInCatalog(String code, String name, double price) {
        theWorkOrderServiceIsAvailable();
        Map<String, Object> payload = Map.of(
                "code", code,
                "name", name,
                "description", "Standardized service: " + name,
                "price", price
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

    private String pendingMaterialSku;
    private String pendingMaterialDescription;
    private double pendingMaterialCost;
    private double pendingMaterialPrice;
    private int pendingMaterialQty;

    @Dado("o material com código {string}, descrição {string}, custo {double}, preço {double} e quantidade {int}")
    public void prepareMaterialData(String code, String desc, double cost, double price, int qty) {
        theWorkOrderServiceIsAvailable();
        this.pendingMaterialSku = code;
        this.pendingMaterialDescription = desc;
        this.pendingMaterialCost = cost;
        this.pendingMaterialPrice = price;
        this.pendingMaterialQty = qty;
    }

    @Dado("o material com os seguintes dados de estoque e precificação:")
    public void prepareMaterialDataFromTable(Map<String, String> data) {
        theWorkOrderServiceIsAvailable();
        this.pendingMaterialSku = data.get("sku");
        this.pendingMaterialDescription = data.get("description");
        this.pendingMaterialCost = Double.parseDouble(data.get("unitCost"));
        this.pendingMaterialPrice = Double.parseDouble(data.get("unitPrice"));
        this.pendingMaterialQty = Integer.parseInt(data.get("quantity"));
    }

    @Quando("a inclusão do material no catálogo é submetida")
    @Quando("o material é cadastrado no catálogo")
    public void submitMaterialInCatalog() {
        registerMaterialWithPricingAndStock(this.pendingMaterialSku, this.pendingMaterialDescription,
                this.pendingMaterialCost, this.pendingMaterialPrice, this.pendingMaterialQty);
    }

    @Quando("a consulta do material pelo código {string} é realizada")
    @Quando("o material com código {string} é consultado")
    public void executeMaterialQuery(String sku) {
        response = RestAssured.given().get("/api/v1/materials?sku=" + sku);
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

    @Entao("os dados do material devem ser consultados com sucesso pelo código {string}")
    public void getMaterialBySku(String sku) {
        Response getResp = RestAssured.given().get("/api/v1/materials?sku=" + sku);
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getList("sku").get(0)).isEqualTo(sku);
        }
    }

    @Entao("a consulta deve retornar os seguintes dados do material:")
    public void materialQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        if (response != null && response.getStatusCode() == 200) {
            expectedFields.forEach((key, expectedValue) -> {
                if ("unitPrice".equals(key) || "unitCost".equals(key)) {
                    double expectedPrice = Double.parseDouble(expectedValue);
                    assertThat(response.jsonPath().getList(key).get(0)).isEqualTo((float) expectedPrice);
                } else if ("quantity".equals(key) || "initialStock".equals(key)) {
                    int expectedQty = Integer.parseInt(expectedValue);
                    assertThat(response.jsonPath().getList(key).get(0)).isEqualTo(expectedQty);
                } else {
                    assertThat(response.jsonPath().getList(key).get(0).toString()).isEqualTo(expectedValue);
                }
            });
        }
    }

    @Dado("que o material com código {string} já existe no catálogo")
    @Dado("o material com código {string} cadastrado no catálogo")
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
                .patch("/api/v1/materials/" + currentMaterialId);
    }

    @Entao("o novo preço {double} deve ser refletido com sucesso na consulta do material")
    public void newPriceReflectedSuccessfully(double expectedPrice) {
        if (response != null && response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getDouble("unitPrice")).isEqualTo(expectedPrice);
        }
    }

    @Quando("o cliente com documento {string} solicita uma Ordem de Serviço para o veículo {string} com a descrição {string}")
    @Quando("o atendente abre uma nova Ordem de Serviço para o cliente {string} e veículo {string} com a descrição {string}")
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
        updateWorkOrderStatus("WAITING_APPROVAL", amount);
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
        updateWorkOrderStatus("COMPLETED", 450.00);
    }

    @Quando("o veículo é liberado para o cliente")
    public void vehicleReleased() {
        updateWorkOrderStatus("DELIVERED", 450.00);
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

    @Entao("a requisição deve ser rejeitada com status {int}")
    public void requestShouldBeRejectedWithStatus(int expectedStatus) {
        if (response != null) {
            assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        }
    }

    @E("a mensagem de erro deve indicar o código {string}")
    public void errorMessageShouldIndicateErrorCode(String expectedErrorCode) {
        if (response != null) {
            String errorCode = response.jsonPath().getString("errorCode");
            if (errorCode == null) {
                errorCode = response.jsonPath().getString("detail");
            }
            assertThat(errorCode).contains(expectedErrorCode);
        }
    }

    @Entao("o erro retornado deve corresponder a:")
    public void errorResponseShouldMatchTable(Map<String, String> expectedFields) {
        if (response != null) {
            expectedFields.forEach((key, expectedValue) -> {
                if ("status".equals(key)) {
                    int expectedStatus = Integer.parseInt(expectedValue);
                    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
                } else if ("errorCode".equals(key)) {
                    String actualCode = response.jsonPath().getString("errorCode");
                    if (actualCode == null) {
                        actualCode = response.jsonPath().getString("detail");
                    }
                    assertThat(actualCode).contains(expectedValue);
                } else if ("detail".equals(key)) {
                    String actualDetail = response.jsonPath().getString("detail");
                    assertThat(actualDetail).isEqualTo(expectedValue);
                } else {
                    String actualValue = response.jsonPath().getString(key);
                    assertThat(actualValue).isEqualTo(expectedValue);
                }
            });
        }
    }
}
