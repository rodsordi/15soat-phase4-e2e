package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CommonAssertionSteps {

    private final ScenarioContext context;

    @Entao("o erro retornado deve corresponder a:")
    public void errorResponseShouldMatchTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
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

    @Entao("a requisição deve ser rejeitada com status {int}")
    public void requestShouldBeRejectedWithStatus(int expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    @E("a mensagem de erro deve indicar o código {string}")
    public void errorMessageShouldIndicateErrorCode(String expectedErrorCode) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        String errorCode = response.jsonPath().getString("errorCode");
        if (errorCode == null) {
            errorCode = response.jsonPath().getString("detail");
        }
        assertThat(errorCode).contains(expectedErrorCode);
    }

    @Entao("o código de status HTTP da resposta deve ser {int}")
    public void httpStatusCodeShouldBe(int expectedStatus) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }
}
