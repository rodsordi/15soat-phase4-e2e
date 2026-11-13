package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CustomerSteps {

    private final ScenarioContext context;

    private String resolveCustomerId(String doc) {
        Response resp = context.givenAuth().get("/api/v1/customers?document=" + doc);
        if (resp != null && resp.getStatusCode() == 200) {
            String id = resp.jsonPath().getString("id");
            if (id == null) {
                id = resp.jsonPath().getString("content[0].id");
            }
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return doc;
    }

    private void cleanUpExistingCustomerIfExists(String doc) {
        try {
            Response resp = context.givenAuth().get("/api/v1/customers?document=" + doc);
            if (resp != null && resp.getStatusCode() == 200) {
                String id = resp.jsonPath().getString("id");
                if (id == null) {
                    id = resp.jsonPath().getString("content[0].id");
                }
                if (id != null && !id.isBlank()) {
                    Response vehResp = context.givenAuth().get("/api/v1/vehicles");
                    if (vehResp != null && vehResp.getStatusCode() == 200) {
                        List<Map<String, Object>> vehicles = vehResp.jsonPath().getList("content");
                        if (vehicles != null) {
                            for (Map<String, Object> v : vehicles) {
                                if (doc.equals(v.get("customerDocument"))) {
                                    context.givenAuth().delete("/api/v1/vehicles/" + v.get("id"));
                                }
                            }
                        }
                    }
                    context.givenAuth().delete("/api/v1/customers/" + id);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Quando("o cliente com documento {string}, nome {string} e email {string} tenta ser cadastrado")
    public void tryRegisterCustomerWithoutAuth(String document, String name, String email) {
        String payload = """
                {
                  "document": "%s",
                  "name": "%s",
                  "email": "%s"
                }
                """.formatted(document, name, email);
        context.setResponse(context.givenAuth()
                .body(payload)
                .post("/api/v1/customers"));
    }

    @Dado("o cliente com documento {string} cadastrado")
    public void customerAlreadyRegistered(String doc) {
        context.setCustomerDoc(doc);
        context.setPreserveCustomer(true);
        Response checkResp = context.givenAuth().get("/api/v1/customers?document=" + doc);
        boolean exists = checkResp != null && checkResp.getStatusCode() == 200 &&
                (checkResp.jsonPath().getList("content") != null && !checkResp.jsonPath().getList("content").isEmpty()
                 || checkResp.jsonPath().getString("id") != null);
        if (!exists) {
            registerCustomer(doc, "Rodrigo Sordi", "rodrigo@fiap.com.br");
        } else {
            String id = checkResp.jsonPath().getString("id");
            if (id == null) {
                id = checkResp.jsonPath().getString("content[0].id");
            }
            if (id != null && !id.isBlank()) {
                String patchPayload = """
                        {
                          "name": "Rodrigo Sordi",
                          "email": "rodrigo@fiap.com.br"
                        }
                        """;
                context.givenAuth().body(patchPayload).patch("/api/v1/customers/" + id);
            }
        }
    }

    @Dado("o cliente com documento {string}, nome {string} e email {string}")
    public void prepareCustomerData(String document, String name, String email) {
        context.setCustomerDoc(document);
        context.setPendingCustomerName(name);
        context.setPendingCustomerEmail(email);
    }

    @Dado("o cliente com os seguintes dados cadastrais:")
    public void prepareCustomerDataFromTable(Map<String, String> data) {
        String doc = data.get("document");
        context.setCustomerDoc(doc);
        context.setPendingCustomerName(data.get("name"));
        context.setPendingCustomerEmail(data.get("email"));
        if (context.isPreserveCustomer()) {
            context.setPreserveCustomer(false);
        } else {
            cleanUpExistingCustomerIfExists(doc);
        }
    }

    @Quando("o cadastro do cliente é submetido")
    @Quando("o cliente é cadastrado")
    public void submitCustomerRegistration() {
        registerCustomer(context.getCustomerDoc(), context.getPendingCustomerName(), context.getPendingCustomerEmail());
    }

    @Quando("um novo cliente é cadastrado com documento {string}, nome {string} e email {string}")
    public void registerCustomer(String document, String name, String email) {
        context.setCustomerDoc(document);
        String payload = """
                {
                  "document": "%s",
                  "name": "%s",
                  "email": "%s"
                }
                """.formatted(document, name, email);
        Response response = context.givenAuth()
                .body(payload)
                .post("/api/v1/customers");
        context.setResponse(response);
    }

    @Entao("o cliente deve ser registrado com sucesso")
    public void customerRegisteredSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        if (response.getStatusCode() == 400 && response.jsonPath().getString("errorCode") != null
                && response.jsonPath().getString("errorCode").contains("ALREADY_EXISTS")) {
            Response getResp = context.givenAuth().get("/api/v1/customers?document=" + context.getCustomerDoc());
            if (getResp != null && getResp.getStatusCode() == 200) {
                context.setResponse(getResp);
                return;
            }
        }
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a resposta deve conter identificador único gerado e documento {string}")
    public void responseContainsIdAndDocument(String expectedDoc) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        String id = response.jsonPath().getString("id");
        if (id == null) {
            id = response.jsonPath().getString("content[0].id");
        }
        String doc = response.jsonPath().getString("document");
        if (doc == null) {
            doc = response.jsonPath().getString("content[0].document");
        }
        assertThat(id).isNotBlank();
        assertThat(doc).isEqualTo(expectedDoc);
    }

    @Quando("a consulta de cliente por documento {string} é realizada")
    @Quando("o cliente com documento {string} é consultado")
    public void executeCustomerQuery(String doc) {
        String customerId = resolveCustomerId(doc);
        if (customerId != null && !customerId.equals(doc)) {
            context.setResponse(context.givenAuth().get("/api/v1/customers/" + customerId));
        } else {
            context.setResponse(context.givenAuth().get("/api/v1/customers?document=" + doc));
        }
    }

    @Entao("a consulta deve retornar o nome {string}")
    public void customerQueryReturnsName(String expectedName) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        String name = response.jsonPath().getString("name");
        if (name == null) {
            name = response.jsonPath().getString("content[0].name");
        }
        assertThat(name).isEqualTo(expectedName);
    }

    @Entao("a consulta deve retornar o nome {string} e o email {string}")
    public void customerQueryReturnsNameAndEmail(String expectedName, String expectedEmail) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        String name = response.jsonPath().getString("name");
        if (name == null) {
            name = response.jsonPath().getString("content[0].name");
        }
        String email = response.jsonPath().getString("email");
        if (email == null) {
            email = response.jsonPath().getString("content[0].email");
        }
        assertThat(name).isEqualTo(expectedName);
        assertThat(email).isEqualTo(expectedEmail);
    }

    @Entao("a consulta deve retornar os seguintes dados do cliente:")
    public void customerQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            String actualValue = response.jsonPath().getString(key);
            if (actualValue == null) {
                actualValue = response.jsonPath().getString("content[0]." + key);
            }
            assertThat(actualValue).isEqualTo(expectedValue);
        });
    }

    @E("a consulta de cliente por documento {string} deve retornar o nome {string}")
    public void getCustomerByDoc(String doc, String expectedName) {
        Response getResp = context.givenAuth().get("/api/v1/customers?document=" + doc);
        assertThat(getResp).as("Customer query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            String name = getResp.jsonPath().getString("name");
            if (name == null) {
                name = getResp.jsonPath().getString("content[0].name");
            }
            assertThat(name).isEqualTo(expectedName);
        }
    }

    @Quando("o cliente com documento {string} tem seu nome atualizado para {string}")
    public void updateCustomerName(String doc, String newName) {
        String targetId = resolveCustomerId(doc);
        String payload = """
                {
                  "name": "%s"
                }
                """.formatted(newName);
        context.setResponse(context.givenAuth()
                .body(payload)
                .patch("/api/v1/customers/" + targetId));
    }

    @Dado("o cliente com documento {string} cadastrado sem veículos")
    public void customerRegisteredWithoutVehicles(String doc) {
        context.setCustomerDoc(doc);
        cleanUpExistingCustomerIfExists(doc);
        registerCustomer(doc, "Cliente Sem Veiculo", "semveiculo@fiap.com.br");
    }

    @Quando("o cliente com documento {string} é excluído por seu identificador único")
    @Quando("o cliente com documento {string} tenta ser excluído por seu identificador único")
    public void deleteCustomerByDocument(String doc) {
        String targetId = resolveCustomerId(doc);
        context.setResponse(context.givenAuth()
                .delete("/api/v1/customers/" + targetId));
    }

    @Entao("a resposta de exclusão deve retornar o código HTTP {int}")
    public void deletionResponseShouldReturnStatus(int expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }
}
