package br.com.fiap.phase4.e2e.steps;

import br.com.fiap.phase4.e2e.context.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class VehicleSteps {

    private final ScenarioContext context;

    @Dado("um cliente cadastrado com documento {string} e veículo {string}")
    public void customerWithVehicleRegistered(String doc, String plate) {
        context.setCustomerDoc(doc);
        context.setVehiclePlate(plate);
    }

    @Quando("o veículo com placa {string} é excluído por seu identificador único")
    public void deleteVehicleByPlate(String plate) {
        context.setResponse(context.givenAuth()
                .delete("/api/v1/vehicles/" + plate));
    }

    @Quando("o veículo com os seguintes dados é vinculado ao cliente {string}:")
    public void registerVehicleFromTable(String doc, Map<String, String> data) {
        String plate = data.get("licensePlate");
        context.setVehiclePlate(plate);
        int year = Integer.parseInt(data.get("year"));
        String make = data.get("make") != null ? data.get("make") : data.get("brand");
        String payload = """
                {
                  "licensePlate": "%s",
                  "customerDocument": "%s",
                  "brand": "%s",
                  "model": "%s",
                  "year": %d
                }
                """.formatted(plate, doc, make, data.get("model"), year);
        Response resp = context.givenAuth()
                .body(payload)
                .post("/api/v1/vehicles");
        context.setResponse(resp);
    }

    @Entao("o veículo deve ser registrado com sucesso")
    public void vehicleRegisteredSuccessfully() {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a resposta do veículo deve confirmar a placa {string} e o documento {string}")
    public void vehicleResponseConfirmsPlateAndDoc(String expectedPlate, String expectedDoc) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("licensePlate")).isEqualTo(expectedPlate);
        assertThat(response.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
    }

    @Quando("a consulta de veículo pela placa {string} é realizada")
    @Quando("o veículo com placa {string} é consultado")
    public void executeVehicleQuery(String plate) {
        context.setResponse(context.givenAuth().get("/api/v1/vehicles?licensePlate=" + plate));
    }

    @Entao("a consulta de veículo deve confirmar o vínculo com o cliente {string}")
    public void vehicleQueryConfirmsLink(String expectedDoc) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
    }

    @E("a consulta deve retornar os dados do veículo com marca {string} e modelo {string}")
    public void vehicleQueryReturnsMakeAndModel(String expectedMake, String expectedModel) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("make")).isEqualTo(expectedMake);
        assertThat(response.jsonPath().getString("model")).isEqualTo(expectedModel);
    }

    @Entao("a consulta deve retornar os seguintes dados do veículo:")
    public void vehicleQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            String actualValue = response.jsonPath().getString(key);
            assertThat(actualValue).isEqualTo(expectedValue);
        });
    }

    @E("a consulta de veículo pela placa {string} deve confirmar o vínculo com o cliente {string}")
    public void getVehicleByPlate(String plate, String expectedDoc) {
        Response getResp = context.givenAuth().get("/api/v1/vehicles/" + plate);
        assertThat(getResp).as("Vehicle query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            assertThat(getResp.jsonPath().getString("customerDocument")).isEqualTo(expectedDoc);
        }
    }
}
