
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:8765";
    }

    @Test
    public void returnPatients(){
        String loginPayload = """
                    {
                        "email":"akstarx47@gmail.com",
                        "password":"Akstar@47"
                    }
                """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth-service/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");


        given()
                .header("Authorization","Bearer " + token)
                .when()
                .get("/patient-service/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());
    }
}
