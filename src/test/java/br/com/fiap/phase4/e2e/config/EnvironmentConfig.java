package br.com.fiap.phase4.e2e.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvironmentConfig {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
    private static final Properties properties = new Properties();
    private static String activeEnv;

    static {
        loadProperties();
    }

    private EnvironmentConfig() {
    }

    public static synchronized void loadProperties() {
        properties.clear();

        // 1. Load base application.properties
        loadResource("application.properties");

        // 2. Identify active environment ('local' by default, or overridden via -Denv or E2E_ENV)
        activeEnv = System.getProperty("env",
                System.getenv().getOrDefault("E2E_ENV", properties.getProperty("e2e.env", "local"))).toLowerCase();

        // 3. Load profile-specific application-{env}.properties
        loadResource("application-" + activeEnv + ".properties");
    }

    private static void loadResource(String filename) {
        try (InputStream in = EnvironmentConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (in != null) {
                Properties temp = new Properties();
                temp.load(in);
                temp.forEach((k, v) -> properties.put(k.toString(), resolvePlaceholders(v.toString())));
            }
        } catch (Exception e) {
            System.err.println("[WARN] EnvironmentConfig failed to load resource '" + filename + "': " + e.getMessage());
        }
    }

    public static String resolvePlaceholders(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2) != null ? matcher.group(2) : "";

            String resolvedValue = System.getProperty(varName,
                    System.getenv().getOrDefault(varName, properties.getProperty(varName, defaultValue)));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedValue));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String getProperty(String key, String defaultValue) {
        String fromSystem = System.getProperty(key, System.getenv(key));
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        String propVal = properties.getProperty(key);
        return propVal != null ? resolvePlaceholders(propVal) : defaultValue;
    }

    public static String getActiveEnvironment() {
        return activeEnv;
    }

    public static boolean isLocalEnvironment() {
        return "local".equalsIgnoreCase(activeEnv);
    }

    public static boolean isPrdEnvironment() {
        return "prd".equalsIgnoreCase(activeEnv);
    }

    public static String getGatewayUrl() {
        return getProperty("gateway.url", "http://localhost:8080");
    }

    public static String getGatewayHost() {
        return getProperty("gateway.host", "");
    }

    public static String getWorkOrderBaseUrl() {
        if (isPrdEnvironment()) {
            return getGatewayUrl();
        }
        return getProperty("workorder.url", "http://localhost:8080");
    }

    public static String getBillingBaseUrl() {
        if (isPrdEnvironment()) {
            return getGatewayUrl();
        }
        return getProperty("billing.url", "http://localhost:8081");
    }

    public static String getExecBaseUrl() {
        if (isPrdEnvironment()) {
            return getGatewayUrl();
        }
        return getProperty("exec.url", "http://localhost:8083");
    }

    public static String getAuthLambdaBaseUrl() {
        if (isPrdEnvironment()) {
            return getGatewayUrl();
        }
        return getProperty("auth.lambda.url", "http://localhost:8080");
    }

    public static String getKeycloakUrl() {
        return getProperty("keycloak.url", "http://localhost:8085");
    }

    public static String getDefaultAuthToken() {
        return getProperty("auth.token", "Bearer mock_employee_jwt_token");
    }

    public static String getAdminAuthToken() {
        return getProperty("admin.token", "Bearer admin_master_token");
    }
}
