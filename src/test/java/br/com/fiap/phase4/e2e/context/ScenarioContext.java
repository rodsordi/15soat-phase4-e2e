package br.com.fiap.phase4.e2e.context;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioContext {

    static {
        RestAssured.filters(new RequestLoggingFilter(LogDetail.ALL), new ResponseLoggingFilter(LogDetail.ALL));
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 5000)
                        .setParam("http.socket.timeout", 10000));
    }

    private Response response;
    private String workOrderId;
    private String invoiceId;
    private String externalPaymentId;
    private String executionId;
    private String customerDoc;
    private String vehiclePlate;
    private String pendingCustomerName;
    private String pendingCustomerEmail;
    private String currentMaterialId;
    private String technicianId = "TECH-101";
    private String currentAuthToken = EnvironmentConfig.getDefaultAuthToken();
    private String adminToken = EnvironmentConfig.getAdminAuthToken();
    private boolean preserveCustomer;

    public void setResponse(Response response) {
        this.response = response;
        if (response != null && "true".equalsIgnoreCase(response.getHeader("X-Synthetic-Response"))) {
            System.out.println("HTTP/1.1 " + response.getStatusCode() + " (In-Memory Response)");
            System.out.println("Content-Type: " + response.getContentType());
            String body = response.getBody() != null ? response.getBody().asPrettyString() : "";
            if (body != null && !body.isBlank()) {
                System.out.println(body);
            }
        }
    }

    public RequestSpecification givenAuth() {
        return givenAuth(EnvironmentConfig.getWorkOrderBaseUrl());
    }

    public RequestSpecification givenBillingAuth() {
        return givenAuth(EnvironmentConfig.getBillingBaseUrl());
    }

    public RequestSpecification givenExecAuth() {
        return givenAuth(EnvironmentConfig.getExecBaseUrl());
    }

    public RequestSpecification givenAuthLambda() {
        return givenAuth(EnvironmentConfig.getAuthLambdaBaseUrl());
    }

    public RequestSpecification givenAuth(String baseUrl) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON);
        if (currentAuthToken != null) {
            spec.header("Authorization", currentAuthToken);
        }
        String host = EnvironmentConfig.getGatewayHost();
        if (host != null && !host.isBlank()) {
            spec.header("Host", host);
        }
        return spec;
    }
}
