import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIT {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:5004";
    }

    @Test
    public void shouldReturnOK_whenTokenIsValid() {
        // Given
        String loginPayload = """
                {
                    "email": "test@gmail.com",
                    "password": "password123"
                }
                """;

        // When and Then
        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUNAUTHORIZED_whenLoginIsInvalid() {
        // Given
        String loginPayload = """
                {
                    "email": "test@gmail.com",
                    "password": "wrongpassword"
                }
                """;

        // When and Then
        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
