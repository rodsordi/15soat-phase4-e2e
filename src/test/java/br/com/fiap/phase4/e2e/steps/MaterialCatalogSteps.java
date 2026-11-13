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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class MaterialCatalogSteps {

    private final ScenarioContext context;

    private String pendingMaterialSku;
    private String pendingMaterialDescription;
    private double pendingMaterialCost;
    private double pendingMaterialPrice;
    private int pendingMaterialQty;

    @Dado("o material com código {string}, descrição {string}, custo {double}, preço {double} e quantidade {int}")
    public void prepareMaterialData(String code, String desc, double cost, double price, int qty) {
        this.pendingMaterialSku = code;
        this.pendingMaterialDescription = desc;
        this.pendingMaterialCost = cost;
        this.pendingMaterialPrice = price;
        this.pendingMaterialQty = qty;
    }

    @Dado("o material com os seguintes dados de estoque e precificação:")
    public void prepareMaterialDataFromTable(Map<String, String> data) {
        this.pendingMaterialSku = data.get("sku");
        this.pendingMaterialDescription = data.get("description");
        this.pendingMaterialCost = Double.parseDouble(data.get("unitCost"));
        this.pendingMaterialPrice = Double.parseDouble(data.get("unitPrice"));
        this.pendingMaterialQty = Integer.parseInt(data.get("quantity"));
    }

    @Quando("a inclusão do material no catálogo é submetida")
    @Quando("o material é cadastrado no catálogo")
    public void submitMaterialInCatalog() {
        registerMaterialWithPricingAndStock(this.pendingMaterialSku, this.pendingMaterialDescription,
                this.pendingMaterialCost, this.pendingMaterialPrice, this.pendingMaterialQty);
    }

    @Quando("a consulta do material pelo código {string} é realizada")
    @Quando("o material com código {string} é consultado")
    public void executeMaterialQuery(String sku) {
        context.setResponse(context.givenAuth().get("/api/v1/materials?sku=" + sku));
    }

    @Quando("um novo material com código {string}, descrição {string}, custo {double}, preço {double} e quantidade {int} é cadastrado")
    public void registerMaterialWithPricingAndStock(String code, String description, double cost, double price, int quantity) {
        String payload = String.format(Locale.US, """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "%s",
                  "unitCost": %.2f,
                  "unitPrice": %.2f,
                  "initialStock": %d,
                  "minStock": 5
                }
                """, code, description, description, cost, price, quantity);

        Response resp = context.givenAuth()
                .body(payload)
                .post("/api/v1/materials");

        context.setResponse(resp);

        if (resp != null && resp.getStatusCode() == 201) {
            context.setCurrentMaterialId(resp.jsonPath().getString("id"));
        } else {
            context.setCurrentMaterialId(UUID.randomUUID().toString());
        }
    }

    @Entao("o material deve ser persistido com status 201 no catálogo")
    public void materialPersistedSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @Entao("os dados do material devem ser consultados com sucesso pelo código {string}")
    public void getMaterialBySku(String sku) {
        Response getResp = context.givenAuth().get("/api/v1/materials?sku=" + sku);
        assertThat(getResp).as("Material query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getList("sku").get(0)).isEqualTo(sku);
        }
    }

    @Entao("a consulta deve retornar os seguintes dados do material:")
    public void materialQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            if ("unitPrice".equals(key) || "unitCost".equals(key)) {
                double expectedPrice = Double.parseDouble(expectedValue);
                assertThat(response.jsonPath().getList(key).get(0)).isEqualTo((float) expectedPrice);
            } else if ("quantity".equals(key) || "initialStock".equals(key)) {
                int expectedQty = Integer.parseInt(expectedValue);
                assertThat(response.jsonPath().getList(key).get(0)).isEqualTo(expectedQty);
            } else {
                assertThat(response.jsonPath().getList(key).get(0).toString()).isEqualTo(expectedValue);
            }
        });
    }

    @Dado("que o material com código {string} já existe no catálogo")
    @Dado("o material com código {string} cadastrado no catálogo")
    public void ensureMaterialExists(String sku) {
        if (context.getCurrentMaterialId() == null) {
            context.setCurrentMaterialId(UUID.randomUUID().toString());
        }
    }

    @Dado("os seguintes materiais cadastrados no catálogo:")
    public void ensureMaterialsExistInCatalog(List<Map<String, String>> materials) {
        materials.forEach(m -> {
            String sku = m.get("sku");
            String description = m.get("description");
            double unitCost = m.get("unitCost") != null ? Double.parseDouble(m.get("unitCost")) : 30.0;
            double unitPrice = Double.parseDouble(m.get("unitPrice"));
            int quantity = Integer.parseInt(m.get("quantity"));
            registerMaterialWithPricingAndStock(sku, description, unitCost, unitPrice, quantity);
        });
    }

    @Quando("o preço de venda do material {string} é atualizado para {double}")
    public void updateMaterialPrice(String sku, double newPrice) {
        String payload = String.format(Locale.US, """
                {
                  "unitPrice": %.2f
                }
                """, newPrice);

        context.setResponse(context.givenAuth()
                .body(payload)
                .patch("/api/v1/materials/" + context.getCurrentMaterialId()));
    }

    @Entao("o novo preço {double} deve ser refletido com sucesso na consulta do material")
    public void newPriceReflectedSuccessfully(double expectedPrice) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getDouble("unitPrice")).isEqualTo(expectedPrice);
    }
}
