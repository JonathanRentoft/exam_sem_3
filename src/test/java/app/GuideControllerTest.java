package app;

import app.dto.GuideDTO;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class GuideControllerTest extends IntegrationTestBase {

    @BeforeAll
    void setUp() {
        setupTest(7778);
    }

    @AfterAll
    void tearDown() {
        teardownTest();
    }

    @Test
    @DisplayName("Test get all guides")
    void testGetAllGuides() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/guides")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].name", notNullValue())
                .body("[0].email", notNullValue());
    }

    @Test
    @DisplayName("Test get all guides without authentication")
    void testGetAllGuidesUnauthorized() {
        given()
                .when()
                .get("/guides")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Test get guide by ID")
    void testGetGuideById() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/guides/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", notNullValue())
                .body("email", notNullValue())
                .body("phone", notNullValue())
                .body("yearsOfExperience", notNullValue());
    }

    @Test
    @DisplayName("Test get non-existent guide")
    void testGetGuideByIdNotFound() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/guides/999")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("Test create guide")
    void testCreateGuide() {
        GuideDTO newGuide = new GuideDTO();
        newGuide.setName("Test Guide");
        newGuide.setEmail("test@example.com");
        newGuide.setPhone("+45 98 76 54 32");
        newGuide.setYearsOfExperience(3);

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(newGuide)
                .when()
                .post("/guides")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Guide"))
                .body("email", equalTo("test@example.com"))
                .body("yearsOfExperience", equalTo(3));
    }

    @Test
    @DisplayName("Test create guide with missing name")
    void testCreateGuideValidation() {
        GuideDTO invalidGuide = new GuideDTO();
        invalidGuide.setEmail("test@example.com");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(invalidGuide)
                .when()
                .post("/guides")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Test update guide")
    void testUpdateGuide() {
        GuideDTO updateGuide = new GuideDTO();
        updateGuide.setName("Updated Guide Name");
        updateGuide.setEmail("updated@example.com");
        updateGuide.setPhone("+45 11 22 33 44");
        updateGuide.setYearsOfExperience(12);

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(updateGuide)
                .when()
                .put("/guides/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Guide Name"))
                .body("yearsOfExperience", equalTo(12));
    }

    @Test
    @DisplayName("Test update non-existent guide")
    void testUpdateGuideNotFound() {
        GuideDTO updateGuide = new GuideDTO();
        updateGuide.setName("Test");
        updateGuide.setEmail("test@test.com");
        updateGuide.setPhone("+45 12 34 56 78");
        updateGuide.setYearsOfExperience(5);

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(updateGuide)
                .when()
                .put("/guides/999")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Test delete guide as user should fail")
    void testDeleteGuideAsUser() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/guides/1")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Test delete guide as admin")
    void testDeleteGuideAsAdmin() {
        // First create a new guide that's not associated with any trips
        GuideDTO newGuide = new GuideDTO();
        newGuide.setName("Delete Test Guide");
        newGuide.setEmail("delete@test.com");
        newGuide.setPhone("+45 99 99 99 99");
        newGuide.setYearsOfExperience(1);

        int guideId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(newGuide)
                .when()
                .post("/guides")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then delete it
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/guides/" + guideId)
                .then()
                .statusCode(204);
    }
}
