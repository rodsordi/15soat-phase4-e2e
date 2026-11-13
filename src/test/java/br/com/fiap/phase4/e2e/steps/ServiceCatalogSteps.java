package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ServiceCatalogSteps {

    private final ScenarioContext context;

    private String pendingServiceCode;
    private String pendingServiceName;
    private double pendingServicePrice;
    private boolean preserveExistingService;

    @Dado("o serviço com código {string}, nome {string} e preço {double}")
    public void prepareServiceData(String code, String name, double price) {
        cleanUpExistingService(code);
        this.pendingServiceCode = code;
        this.pendingServiceName = name;
        this.pendingServicePrice = price;
    }

    @Dado("o serviço com os seguintes dados:")
    public void prepareServiceDataFromTable(Map<String, String> data) {
        this.pendingServiceCode = data.get("code");
        this.pendingServiceName = data.get("name");
        this.pendingServicePrice = Double.parseDouble(data.get("price"));
        if (this.preserveExistingService) {
            this.preserveExistingService = false;
        } else {
            cleanUpExistingService(this.pendingServiceCode);
        }
    }

    @Quando("a inclusão do serviço no catálogo é submetida")
    @Quando("o serviço é cadastrado no catálogo")
    public void submitServiceInCatalog() {
        String payload = String.format(Locale.US, """
                {
                  "code": "%s",
                  "name": "%s",
                  "description": "Standardized service: %s",
                  "price": %.2f
                }
                """, this.pendingServiceCode, this.pendingServiceName, this.pendingServiceName, this.pendingServicePrice);
        context.setResponse(context.givenAuth()
                .body(payload)
                .post("/api/v1/services"));
    }

    @Dado("que o serviço com código {string} já existe no catálogo")
    @Dado("o serviço com código {string} cadastrado no catálogo")
    public void ensureServiceExists(String code) {
        this.preserveExistingService = true;
        cleanUpExistingService(code);
        registerServiceInCatalog(code, "Troca de Óleo e Filtro", 150.00);
    }

    @Dado("os seguintes serviços cadastrados no catálogo:")
    public void ensureServicesExistInCatalog(List<Map<String, String>> services) {
        services.forEach(s -> {
            String code = s.get("code");
            String name = s.get("name");
            double price = Double.parseDouble(s.get("price"));
            registerServiceInCatalog(code, name, price);
        });
    }

    private Object extractFirstField(Response response, String field) {
        List<Object> contentList = response.jsonPath().getList("content." + field);
        if (contentList != null && !contentList.isEmpty()) {
            return contentList.get(0);
        }
        List<Object> directList = response.jsonPath().getList(field);
        if (directList != null && !directList.isEmpty()) {
            return directList.get(0);
        }
        return response.jsonPath().get(field);
    }

    private void cleanUpExistingService(String code) {
        try {
            Response resp = context.givenAuth().get("/api/v1/services?code=" + code);
            if (resp != null && resp.getStatusCode() == 200) {
                List<String> ids = resp.jsonPath().getList("content.id");
                if (ids != null) {
                    for (String id : ids) {
                        context.givenAuth().delete("/api/v1/services/" + id);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Quando("a consulta do serviço pelo código {string} é realizada")
    @Quando("o serviço com código {string} é consultado")
    public void executeServiceQuery(String code) {
        context.setResponse(context.givenAuth().get("/api/v1/services?code=" + code));
    }

    @Entao("a consulta do serviço deve retornar o preço {double}")
    public void serviceQueryReturnsPrice(double expectedPrice) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        Object val = extractFirstField(response, "price");
        assertThat(val).isNotNull();
        assertThat(((Number) val).doubleValue()).isEqualTo(expectedPrice);
    }

    @Entao("a consulta deve retornar os seguintes dados do serviço:")
    public void serviceQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            Object val = extractFirstField(response, key);
            assertThat(val).as("Field " + key + " in service response").isNotNull();
            if ("price".equals(key)) {
                double expectedPrice = Double.parseDouble(expectedValue);
                assertThat(((Number) val).doubleValue()).isEqualTo(expectedPrice);
            } else {
                assertThat(val.toString()).isEqualTo(expectedValue);
            }
        });
    }

    @Quando("um novo serviço com código {string}, nome {string} e preço {double} é cadastrado")
    public void registerServiceInCatalog(String code, String name, double price) {
        cleanUpExistingService(code);
        String payload = String.format(Locale.US, """
                {
                  "code": "%s",
                  "name": "%s",
                  "description": "Standardized service: %s",
                  "price": %.2f
                }
                """, code, name, name, price);
        context.setResponse(context.givenAuth()
                .body(payload)
                .post("/api/v1/services"));
    }

    @Entao("o serviço deve ser registrado com sucesso no catálogo")
    public void serviceRegisteredSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a consulta do serviço pelo código {string} deve retornar o preço {double}")
    public void getServiceByCode(String code, double expectedPrice) {
        Response getResp = context.givenAuth().get("/api/v1/services?code=" + code);
        assertThat(getResp).as("Service query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getList("price").get(0)).isEqualTo((float) expectedPrice);
        }
    }
}
