package br.com.fiap.phase4.e2e.context;

import br.com.fiap.phase4.e2e.config.EnvironmentConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioContext {

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
    private String currentAuthToken = "Bearer mock_employee_jwt_token";
    private String adminToken = "Bearer admin_master_token";
    private boolean preserveCustomer;

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
