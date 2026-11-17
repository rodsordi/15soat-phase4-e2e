package br.com.fiap.phase4.e2e.auth;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized authentication service providing authentic RS256 JWT tokens
 * issued by the Serverless Auth Lambda for end-to-end testing in production.
 */
public final class AuthTokenService {

    private static final Map<String, String> TOKEN_CACHE = new ConcurrentHashMap<>();

    private AuthTokenService() {
    }

    public static String getTokenForRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String normalizedRole = normalizeRole(role);

        if (EnvironmentConfig.isLocalEnvironment()) {
            return "Bearer mock_" + normalizedRole.toLowerCase() + "_jwt_token";
        }

        return TOKEN_CACHE.computeIfAbsent(normalizedRole, r -> {
            String token = requestTokenFromLambda(r);
            if (token != null) {
                return "Bearer " + token;
            }
            return EnvironmentConfig.getDefaultAuthToken();
        });
    }

    private static String normalizeRole(String role) {
        String upper = role.trim().toUpperCase();
        return switch (upper) {
            case "ADMIN", "ADMINISTRATOR" -> "ADMIN";
            case "CUSTOMER", "CLIENT", "USER" -> "CUSTOMER";
            case "EMPLOYEE", "ATTENDANT", "MECHANIC", "BILLING_ANALYST" -> "EMPLOYEE";
            default -> upper;
        };
    }

    private static String requestTokenFromLambda(String role) {
        String username;
        String password;

        switch (role) {
            case "ADMIN" -> {
                username = "admin_user";
                password = "AdminPassword123!";
            }
            case "EMPLOYEE" -> {
                username = "86266070087";
                password = "StrongP@ss2026";
            }
            case "CUSTOMER" -> {
                username = "52998224725";
                password = "CustomerPass123!";
            }
            default -> {
                username = "86266070087";
                password = "StrongP@ss2026";
            }
        }

        try {
            Response response = RestAssured.given()
                    .baseUri(EnvironmentConfig.getAuthLambdaBaseUrl())
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                              "username": "%s",
                              "password": "%s"
                            }
                            """.formatted(username, password))
                    .post("/auth/login");

            if (response.getStatusCode() == 200) {
                String token = response.jsonPath().getString("access_token");
                if (token != null && !token.isBlank()) {
                    return token;
                }
            } else {
                System.err.printf("[WARN] AuthTokenService: Failed to get token for role %s. HTTP Status: %d%n",
                        role, response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.printf("[WARN] AuthTokenService: Exception while requesting token for role %s: %s%n",
                    role, e.getMessage());
        }

        return null;
    }

    public static void clearCache() {
        TOKEN_CACHE.clear();
    }
}
