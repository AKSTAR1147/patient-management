import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:8765";
    }


    //importatn steps of testing
    //1. Arrange
    //2. Act
    //3. Assert
    @Test
    public void returnValidTokenResponse(){
        String loginPayload = """
                    {
                        "email":"akstarx47@gmail.com",
                        "password":"Akstar@47"
                    }
                """;

        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth-service/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Genearted Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void returnInvalidUnauthorizedWrongEmail(){
        String loginPayload = """
                    {
                        "email":"akstarx47@gmaill.com",
                        "password":"Wrong@47"
                    }
                """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth-service/login")
                .then()
                .statusCode(401);
    }
}
