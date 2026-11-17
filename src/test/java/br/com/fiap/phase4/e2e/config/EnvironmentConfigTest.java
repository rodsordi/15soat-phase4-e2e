package br.com.fiap.phase4.e2e.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnvironmentConfig - Multi-Environment Profile Configuration Tests")
class EnvironmentConfigTest {

    @AfterEach
    void resetEnvironment() {
        System.clearProperty("env");
        System.clearProperty("gateway.url");
        System.clearProperty("gateway.host");
        System.clearProperty("workorder.url");
        System.clearProperty("billing.url");
        System.clearProperty("exec.url");
        EnvironmentConfig.loadProperties();
    }

    @Nested
    @DisplayName("Local Profile Tests")
    class LocalProfileTests {

        @Test
        @DisplayName("Should activate local environment by default and resolve microservice discrete URLs")
        void shouldActivateLocalEnvironmentByDefault() {
            System.setProperty("env", "local");
            EnvironmentConfig.loadProperties();

            assertThat(EnvironmentConfig.getActiveEnvironment()).isEqualTo("local");
            assertThat(EnvironmentConfig.isLocalEnvironment()).isTrue();
            assertThat(EnvironmentConfig.isPrdEnvironment()).isFalse();

            assertThat(EnvironmentConfig.getWorkOrderBaseUrl()).isEqualTo("http://localhost:8080");
            assertThat(EnvironmentConfig.getBillingBaseUrl()).isEqualTo("http://localhost:8081");
            assertThat(EnvironmentConfig.getExecBaseUrl()).isEqualTo("http://localhost:8083");
            assertThat(EnvironmentConfig.getKeycloakUrl()).isEqualTo("http://localhost:8085");
            assertThat(EnvironmentConfig.getDefaultAuthToken()).isNotBlank();
            assertThat(EnvironmentConfig.getAdminAuthToken()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("PRD Profile Tests")
    class PrdProfileTests {

        @Test
        @DisplayName("Should activate prd environment and route microservices through API Gateway")
        void shouldActivatePrdEnvironmentAndRouteViaGateway() {
            System.setProperty("env", "prd");
            EnvironmentConfig.loadProperties();

            assertThat(EnvironmentConfig.getActiveEnvironment()).isEqualTo("prd");
            assertThat(EnvironmentConfig.isPrdEnvironment()).isTrue();
            assertThat(EnvironmentConfig.isLocalEnvironment()).isFalse();

            String gatewayUrl = EnvironmentConfig.getGatewayUrl();
            assertThat(EnvironmentConfig.getWorkOrderBaseUrl()).isEqualTo(gatewayUrl);
            assertThat(EnvironmentConfig.getBillingBaseUrl()).isEqualTo(gatewayUrl);
            assertThat(EnvironmentConfig.getExecBaseUrl()).isEqualTo(gatewayUrl);
            assertThat(EnvironmentConfig.getAuthLambdaBaseUrl()).isEqualTo(EnvironmentConfig.getProperty("auth.lambda.url", "https://6e6fze5u46.execute-api.us-east-2.amazonaws.com"));
            assertThat(EnvironmentConfig.getGatewayHost()).isEqualTo("api.garage.fiap.com.br");
        }

        @Test
        @DisplayName("Should allow overriding gateway URL in PRD environment via system properties")
        void shouldAllowOverridingGatewayUrlViaSystemProperty() {
            System.setProperty("env", "prd");
            System.setProperty("gateway.url", "https://custom-gateway.aws.com");
            EnvironmentConfig.loadProperties();

            assertThat(EnvironmentConfig.getGatewayUrl()).isEqualTo("https://custom-gateway.aws.com");
            assertThat(EnvironmentConfig.getWorkOrderBaseUrl()).isEqualTo("https://custom-gateway.aws.com");
        }
    }

    @Nested
    @DisplayName("Placeholder Resolution Tests")
    class PlaceholderResolutionTests {

        @Test
        @DisplayName("Should resolve placeholder with default fallback value")
        void shouldResolvePlaceholderWithDefaultValue() {
            String resolved = EnvironmentConfig.resolvePlaceholders("${NON_EXISTENT_VAR:my-default-value}");
            assertThat(resolved).isEqualTo("my-default-value");
        }

        @Test
        @DisplayName("Should resolve placeholder from system properties when present")
        void shouldResolvePlaceholderFromSystemProperty() {
            System.setProperty("MY_TEST_KEY", "resolved-value");
            try {
                String resolved = EnvironmentConfig.resolvePlaceholders("${MY_TEST_KEY:fallback}");
                assertThat(resolved).isEqualTo("resolved-value");
            } finally {
                System.clearProperty("MY_TEST_KEY");
            }
        }
    }
}
