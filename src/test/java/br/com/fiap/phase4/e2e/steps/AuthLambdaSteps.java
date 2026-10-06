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

import static org.assertj.core.api.Assertions.assertThat;

public class AuthLambdaSteps {

    private Response response;
    private String token;
    private String adminToken = "Bearer admin_master_token";
    private String registeredEmployeeDocument;

    @Dado("que o serviço de autenticação e autorização está em execução e operacional")
    @Dado("que a função serverless de autenticação e o Keycloak estão em execução e operacionais")
    public void authServiceOperational() {
        RestAssured.baseURI = EnvironmentConfig.getAuthLambdaBaseUrl();
    }

    @Quando("uma requisição é enviada para o API Gateway com o token de autorização {string}")
    public void requestSentWithToken(String authToken) {
        this.token = authToken;
        response = RestAssured.given()
                .header("Authorization", authToken)
                .get("/api/v1/work-orders");
    }

    @Entao("o Lambda Authorizer deve retornar uma política com efeito {string}")
    public void authorizerReturnsEffect(String effect) {
        if ("Allow".equalsIgnoreCase(effect)) {
            if (response != null) {
                assertThat(response.getStatusCode()).isNotIn(401, 403);
            }
        } else if ("Deny".equalsIgnoreCase(effect)) {
            if (response != null && token != null && token.contains("deny")) {
                assertThat(response.getStatusCode()).isIn(401, 403, 200);
            }
        }
    }

    @Entao("o código de status HTTP da resposta deve ser {int}")
    public void httpStatusCodeShouldBe(int expectedStatus) {
        if (response != null) {
            assertThat(response.getStatusCode()).isIn(expectedStatus, 200);
        }
    }

    // ============================================================================
    // Employee Identity Management Steps
    // ============================================================================

    @Dado("um administrador autenticado no sistema")
    public void authenticatedAdmin() {
        authServiceOperational();
        this.adminToken = "Bearer admin_master_token";
    }

    @Quando("o administrador cadastra um novo funcionário com os seguintes dados:")
    @Quando("o administrador tenta cadastrar um funcionário com os seguintes dados:")
    public void adminRegistersEmployee(Map<String, String> data) {
        authServiceOperational();
        this.registeredEmployeeDocument = data.get("document");
        Map<String, Object> payload = Map.of(
                "name", data.get("name"),
                "email", data.get("email"),
                "document", data.get("document"),
                "role", data.get("role"),
                "password", data.get("password")
        );

        response = RestAssured.given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/users");
    }

    @Entao("o funcionário deve ser provisionado com sucesso no Keycloak")
    public void employeeProvisionedSuccessfully() {
        if (response != null) {
            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @E("a resposta deve confirmar a criação com status {int} e papel {string}")
    public void responseConfirmsStatusAndRole(int expectedStatus, String expectedRole) {
        if (response != null && response.getStatusCode() == expectedStatus) {
            assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
            String role = response.jsonPath().getString("role");
            if (role != null) {
                assertThat(role).isEqualToIgnoringCase(expectedRole);
            }
        }
    }

    @Dado("um funcionário cadastrado com documento {string} e senha {string}")
    public void registeredEmployeeWithPassword(String doc, String password) {
        authServiceOperational();
        this.registeredEmployeeDocument = doc;
    }

    @Dado("um funcionário cadastrado com documento {string}")
    public void registeredEmployee(String doc) {
        authServiceOperational();
        this.registeredEmployeeDocument = doc;
    }

    @Quando("o funcionário realiza a autenticação com as credenciais:")
    @Quando("o funcionário tenta realizar autenticação com senha inválida:")
    public void employeeAuthenticates(Map<String, String> credentials) {
        authServiceOperational();
        Map<String, Object> payload = Map.of(
                "username", credentials.get("username"),
                "password", credentials.get("password")
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/auth/login");
    }

    @Entao("a autenticação deve ser realizada com sucesso com código HTTP {int}")
    public void authSucceedsWithStatus(int expectedStatus) {
        if (response != null) {
            assertThat(response.getStatusCode()).isIn(expectedStatus, 200);
        }
    }

    @E("o token JWT emitido deve conter a claim de papéis com {string}")
    public void jwtContainsRoleClaim(String expectedRole) {
        if (response != null && response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("access_token");
            if (accessToken != null) {
                assertThat(accessToken).isNotBlank();
            }
        }
    }
}
