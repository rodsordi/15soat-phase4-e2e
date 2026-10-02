package br.com.fiap.phase4.e2e.config;

public class EnvironmentConfig {

    private static final String ENV = System.getProperty("env", "local");
    private static final String GATEWAY_URL = System.getProperty("gateway.url",
            System.getenv().getOrDefault("API_GATEWAY_URL", "http://localhost:8000"));

    public static String getWorkOrderBaseUrl() {
        if ("prd".equalsIgnoreCase(ENV)) {
            return GATEWAY_URL;
        }
        return System.getProperty("workorder.url", "http://localhost:8080");
    }

    public static String getBillingBaseUrl() {
        if ("prd".equalsIgnoreCase(ENV)) {
            return GATEWAY_URL;
        }
        return System.getProperty("billing.url", "http://localhost:8081");
    }

    public static String getExecBaseUrl() {
        if ("prd".equalsIgnoreCase(ENV)) {
            return GATEWAY_URL;
        }
        return System.getProperty("exec.url", "http://localhost:8083");
    }

    public static String getActiveEnvironment() {
        return ENV;
    }
}
