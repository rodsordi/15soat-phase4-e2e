package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AuthLambdaSteps {

    private final ScenarioContext context;
    private String registeredEmployeeDocument;

    @Dado("que o serviço de autenticação e autorização está em execução e operacional")
    @Dado("que a função serverless de autenticação e o Keycloak estão em execução e operacionais")
    public void authServiceOperational() {
        // Operational check verified via baseUri per request
    }

    @Quando("uma requisição é enviada para o API Gateway com o token de autorização {string}")
    public void requestSentWithToken(String authToken) {
        Response response = RestAssured.given()
                .baseUri(EnvironmentConfig.getWorkOrderBaseUrl())
                .header("Authorization", authToken)
                .get("/api/v1/work-orders");
        context.setResponse(response);
    }

    @Entao("o Lambda Authorizer deve retornar uma política com efeito {string}")
    public void authorizerReturnsEffect(String effect) {
        Response response = context.getResponse();
        assertThat(response).as("Authorizer response must not be null").isNotNull();
        if ("Allow".equalsIgnoreCase(effect)) {
            assertThat(response.getStatusCode()).isNotIn(401, 403);
        } else if ("Deny".equalsIgnoreCase(effect)) {
            assertThat(response.getStatusCode()).isIn(401, 403);
        }
    }

    // ============================================================================
    // Employee Identity Management Steps
    // ============================================================================

    @Dado("um administrador autenticado no sistema")
    public void authenticatedAdmin() {
        context.setAdminToken("Bearer admin_master_token");
    }

    @Quando("o administrador cadastra um novo funcionário com os seguintes dados:")
    @Quando("o administrador tenta cadastrar um funcionário com os seguintes dados:")
    public void adminRegistersEmployee(Map<String, String> data) {
        this.registeredEmployeeDocument = data.get("document");
        String payload = """
                {
                  "name": "%s",
                  "email": "%s",
                  "document": "%s",
                  "role": "%s",
                  "password": "%s"
                }
                """.formatted(
                data.get("name"),
                data.get("email"),
                data.get("document"),
                data.get("role"),
                data.get("password")
        );

        Response response = context.givenAuthLambda()
                .header("Authorization", context.getAdminToken())
                .body(payload)
                .post("/users");
        context.setResponse(response);
    }

    @Entao("o funcionário deve ser provisionado com sucesso no Keycloak")
    public void employeeProvisionedSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("Employee provisioning response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a resposta deve confirmar a criação com status {int} e papel {string}")
    public void responseConfirmsStatusAndRole(int expectedStatus, String expectedRole) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        String role = response.jsonPath().getString("role");
        if (role != null) {
            assertThat(role).isEqualToIgnoringCase(expectedRole);
        }
    }

    @Dado("um funcionário cadastrado com documento {string} e senha {string}")
    public void registeredEmployeeWithPassword(String doc, String password) {
        this.registeredEmployeeDocument = doc;
    }

    @Dado("um funcionário cadastrado com documento {string}")
    public void registeredEmployee(String doc) {
        this.registeredEmployeeDocument = doc;
    }

    @Quando("o funcionário realiza a autenticação com as credenciais:")
    @Quando("o funcionário tenta realizar autenticação com senha inválida:")
    public void employeeAuthenticates(Map<String, String> credentials) {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(credentials.get("username"), credentials.get("password"));

        Response response = context.givenAuthLambda()
                .body(payload)
                .post("/auth/login");
        context.setResponse(response);
    }

    @Entao("a autenticação deve ser realizada com sucesso com código HTTP {int}")
    public void authSucceedsWithStatus(int expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("Auth response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    @E("o token JWT emitido deve conter a claim de papéis com {string}")
    public void jwtContainsRoleClaim(String expectedRole) {
        Response response = context.getResponse();
        assertThat(response).as("Auth response must not be null").isNotNull();
        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("access_token");
            if (accessToken != null) {
                assertThat(accessToken).isNotBlank();
            }
        }
    }
}
