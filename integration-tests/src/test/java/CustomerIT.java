import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CustomerIT {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:5004";
    }

    @Test
    public void shouldReturnCustomers_whenTokenIsValid() {
        // Given
        String loginPayload = """
                {
                    "email": "test@gmail.com",
                    "password": "password123"
                }
                """;

        // When and Then
        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getString("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/customers")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("customers", notNullValue());
    }
}
