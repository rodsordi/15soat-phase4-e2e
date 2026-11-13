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

    private String resolveVehicleId(String plate) {
        Response resp = context.givenAuth().get("/api/v1/vehicles?licensePlate=" + plate);
        if (resp != null && resp.getStatusCode() == 200) {
            String id = resp.jsonPath().getString("id");
            if (id == null) {
                id = resp.jsonPath().getString("content[0].id");
            }
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return plate;
    }

    @Dado("um cliente cadastrado com documento {string} e veículo {string}")
    public void customerWithVehicleRegistered(String doc, String plate) {
        context.setCustomerDoc(doc);
        context.setVehiclePlate(plate);

        Response checkCust = context.givenAuth().get("/api/v1/customers?document=" + doc);
        boolean custExists = checkCust != null && checkCust.getStatusCode() == 200 &&
                (checkCust.jsonPath().getList("content") != null && !checkCust.jsonPath().getList("content").isEmpty()
                 || checkCust.jsonPath().getString("id") != null);
        if (!custExists) {
            String custPayload = """
                    {
                      "document": "%s",
                      "name": "Rodrigo Sordi",
                      "email": "rodrigo@fiap.com.br"
                    }
                    """.formatted(doc);
            context.givenAuth().body(custPayload).post("/api/v1/customers");
        }

        Response checkVeh = context.givenAuth().get("/api/v1/vehicles?licensePlate=" + plate);
        boolean vehExists = checkVeh != null && checkVeh.getStatusCode() == 200 &&
                (checkVeh.jsonPath().getList("content") != null && !checkVeh.jsonPath().getList("content").isEmpty()
                 || checkVeh.jsonPath().getString("id") != null);
        if (!vehExists) {
            String vehPayload = """
                    {
                      "licensePlate": "%s",
                      "customerDocument": "%s",
                      "brand": "Toyota",
                      "model": "Corolla",
                      "year": 2023
                    }
                    """.formatted(plate, doc);
            context.givenAuth().body(vehPayload).post("/api/v1/vehicles");
        }
    }

    @Quando("o veículo com placa {string} é excluído por seu identificador único")
    public void deleteVehicleByPlate(String plate) {
        String targetId = resolveVehicleId(plate);
        context.setResponse(context.givenAuth()
                .delete("/api/v1/vehicles/" + targetId));
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
        if (response.getStatusCode() == 400 && response.jsonPath().getString("errorCode") != null
                && response.jsonPath().getString("errorCode").contains("ALREADY_EXISTS")) {
            Response getResp = context.givenAuth().get("/api/v1/vehicles?licensePlate=" + context.getVehiclePlate());
            if (getResp != null && getResp.getStatusCode() == 200) {
                context.setResponse(getResp);
                return;
            }
        }
        assertThat(response.getStatusCode()).isIn(200, 201);
    }

    @E("a resposta do veículo deve confirmar a placa {string} e o documento {string}")
    public void vehicleResponseConfirmsPlateAndDoc(String expectedPlate, String expectedDoc) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isIn(200, 201);
        String actualPlate = response.jsonPath().getString("licensePlate");
        if (actualPlate == null) {
            actualPlate = response.jsonPath().getString("content[0].licensePlate");
        }
        String actualDoc = response.jsonPath().getString("customerDocument");
        if (actualDoc == null) {
            actualDoc = response.jsonPath().getString("content[0].customerDocument");
        }
        assertThat(actualPlate).isEqualTo(expectedPlate);
        assertThat(actualDoc).isEqualTo(expectedDoc);
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
        String actualDoc = response.jsonPath().getString("customerDocument");
        if (actualDoc == null) {
            actualDoc = response.jsonPath().getString("content[0].customerDocument");
        }
        assertThat(actualDoc).isEqualTo(expectedDoc);
    }

    @E("a consulta deve retornar os dados do veículo com marca {string} e modelo {string}")
    public void vehicleQueryReturnsMakeAndModel(String expectedMake, String expectedModel) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        String make = response.jsonPath().getString("make");
        if (make == null) {
            make = response.jsonPath().getString("content[0].make");
        }
        String model = response.jsonPath().getString("model");
        if (model == null) {
            model = response.jsonPath().getString("content[0].model");
        }
        assertThat(make).isEqualTo(expectedMake);
        assertThat(model).isEqualTo(expectedModel);
    }

    @Entao("a consulta deve retornar os seguintes dados do veículo:")
    public void vehicleQueryReturnsDataFromTable(Map<String, String> expectedFields) {
        Response response = context.getResponse();
        assertThat(response).as("HTTP response must not be null").isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        expectedFields.forEach((key, expectedValue) -> {
            String actualValue = response.jsonPath().getString(key);
            if (actualValue == null) {
                actualValue = response.jsonPath().getString("content[0]." + key);
            }
            assertThat(actualValue).isEqualTo(expectedValue);
        });
    }

    @E("a consulta de veículo pela placa {string} deve confirmar o vínculo com o cliente {string}")
    public void getVehicleByPlate(String plate, String expectedDoc) {
        Response getResp = context.givenAuth().get("/api/v1/vehicles?licensePlate=" + plate);
        assertThat(getResp).as("Vehicle query response must not be null").isNotNull();
        if (getResp.getStatusCode() == 200) {
            String actualDoc = getResp.jsonPath().getString("customerDocument");
            if (actualDoc == null) {
                actualDoc = getResp.jsonPath().getString("content[0].customerDocument");
            }
            assertThat(actualDoc).isEqualTo(expectedDoc);
        }
    }
}
