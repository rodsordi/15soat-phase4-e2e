package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CustomerSteps {

    private final ScenarioContext context;

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

    @Dado("que o cliente com documento {string} já está previamente cadastrado no sistema")
    @Dado("o cliente com documento {string} cadastrado")
    public void customerAlreadyRegistered(String doc) {
        context.setCustomerDoc(doc);
        registerCustomer(doc, "Rodrigo Sordi", "rodrigo@fiap.com.br");
    }

    @Dado("o cliente com documento {string}, nome {string} e email {string}")
    public void prepareCustomerData(String document, String name, String email) {
        context.setCustomerDoc(document);
        context.setPendingCustomerName(name);
        context.setPendingCustomerEmail(email);
    }

    @Dado("o cliente com os seguintes dados cadastrais:")
    public void prepareCustomerDataFromTable(Map<String, String> data) {
        context.setCustomerDoc(data.get("document"));
        context.setPendingCustomerName(data.get("name"));
        context.setPendingCustomerEmail(data.get("email"));
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
        context.setResponse(context.givenAuth()
                .body(payload)
                .post("/api/v1/customers"));
    }

    @Entao("o cliente deve ser registrado com sucesso")
    public void customerRegisteredSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a resposta deve conter identificador único gerado e documento {string}")
    public void responseContainsIdAndDocument(String expectedDoc) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("id")).isNotBlank();
        assertThat(response.jsonPath().getString("document")).isEqualTo(expectedDoc);
    }

    @Quando("a consulta de cliente por documento {string} é realizada")
    @Quando("o cliente com documento {string} é consultado")
    public void executeCustomerQuery(String doc) {
        context.setResponse(context.givenAuth().get("/api/v1/customers?document=" + doc));
    }

    @Entao("a consulta deve retornar o nome {string}")
    public void customerQueryReturnsName(String expectedName) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo(expectedName);
    }

    @Entao("a consulta deve retornar o nome {string} e o email {string}")
    public void customerQueryReturnsNameAndEmail(String expectedName, String expectedEmail) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo(expectedName);
        assertThat(response.jsonPath().getString("email")).isEqualTo(expectedEmail);
    }

    @Entao("a consulta deve retornar os seguintes dados do cliente:")
    public void customerQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            String actualValue = response.jsonPath().getString(key);
            assertThat(actualValue).isEqualTo(expectedValue);
        });
    }

    @E("a consulta de cliente por documento {string} deve retornar o nome {string}")
    public void getCustomerByDoc(String doc, String expectedName) {
        Response getResp = context.givenAuth().get("/api/v1/customers?document=" + doc);
        assertThat(getResp).as("Customer query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getString("name")).isEqualTo(expectedName);
        }
    }

    @Quando("o cliente com documento {string} tem seu nome atualizado para {string}")
    public void updateCustomerName(String doc, String newName) {
        String payload = """
                {
                  "name": "%s"
                }
                """.formatted(newName);
        context.setResponse(context.givenAuth()
                .body(payload)
                .patch("/api/v1/customers/" + doc));
    }

    @Dado("o cliente com documento {string} cadastrado sem veículos")
    public void customerRegisteredWithoutVehicles(String doc) {
        context.setCustomerDoc(doc);
        registerCustomer(doc, "Cliente Sem Veiculo", "semveiculo@fiap.com.br");
    }

    @Quando("o cliente com documento {string} é excluído por seu identificador único")
    @Quando("o cliente com documento {string} tenta ser excluído por seu identificador único")
    public void deleteCustomerByDocument(String doc) {
        context.setResponse(context.givenAuth()
                .delete("/api/v1/customers/" + doc));
    }

    @Entao("a resposta de exclusão deve retornar o código HTTP {int}")
    public void deletionResponseShouldReturnStatus(int expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }
}
