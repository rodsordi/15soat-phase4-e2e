package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

/**
 * Reusable steps for cross-cutting security, OIDC token validation, and defense-in-depth guardrails.
 */
@RequiredArgsConstructor
public class CrossApiSecuritySteps {

    private final ScenarioContext context;

    @Dado("que a requisição é configurada com um token de autenticação malformado")
    public void configureMalformedAuthToken() {
        context.setCurrentAuthToken("Bearer ");
    }

    @Dado("que o usuário está autenticado com perfil {string}")
    @Dado("que a requisição é autenticada com perfil {string}")
    public void configureAuthProfile(String role) {
        context.setCurrentAuthToken("Bearer valid_" + role.toLowerCase() + "_token");
    }

    @Quando("uma requisição {string} for enviada para a API {string} no endpoint {string}")
    public void sendRequestToServiceEndpoint(String httpMethod, String serviceName, String endpoint) {
        String baseUrl = resolveBaseUrl(serviceName);
        RequestSpecification spec = context.givenAuth(baseUrl);

        if ("POST".equalsIgnoreCase(httpMethod) || "PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
            spec.body("{}");
        }

        Response response = switch (httpMethod.toUpperCase()) {
            case "GET" -> spec.get(endpoint);
            case "POST" -> spec.post(endpoint);
            case "PUT" -> spec.put(endpoint);
            case "PATCH" -> spec.patch(endpoint);
            case "DELETE" -> spec.delete(endpoint);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        };

        context.setResponse(response);
    }

    private String resolveBaseUrl(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "work-order", "workorder" -> EnvironmentConfig.getWorkOrderBaseUrl();
            case "billing" -> EnvironmentConfig.getBillingBaseUrl();
            case "exec", "execution" -> EnvironmentConfig.getExecBaseUrl();
            case "auth-lambda", "auth" -> EnvironmentConfig.getAuthLambdaBaseUrl();
            default -> throw new IllegalArgumentException("Unknown service alias: " + serviceName);
        };
    }
}
