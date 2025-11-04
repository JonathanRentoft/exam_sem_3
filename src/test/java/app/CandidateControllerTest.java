package app;

import app.dto.CandidateDTO;
import app.dto.UserDTO;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CandidateControllerTest extends IntegrationTestBase {

    @BeforeAll
    void setUp() {
        setupTest(7777);
    }

    @AfterAll
    void tearDown() {
        teardownTest();
    }

    @Test
    @DisplayName("Test login with valid credentials")
    void testLoginSuccess() {
        UserDTO userDTO = new UserDTO("user", "user123");
        given()
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("user"));
    }

    @Test
    @DisplayName("Test login with invalid credentials")
    void testLoginFailure() {
        UserDTO userDTO = new UserDTO("user", "wrongpassword");
        given()
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid"));
    }

    @Test
    @DisplayName("Test get all candidates")
    void testGetAllCandidates() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/candidates")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].name", notNullValue());
    }

    @Test
    @DisplayName("Test get all candidates without authentication")
    void testGetAllCandidatesUnauthorized() {
        given()
                .when()
                .get("/candidates")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Test get candidates by skill category")
    void testGetCandidatesByCategory() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .queryParam("category", "PROG_LANG")
                .when()
                .get("/candidates")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Test get candidate by ID - external API will be called")
    void testGetCandidateById() {

    }

    @Test
    @DisplayName("Test get non-existent candidate")
    void testGetCandidateByIdNotFound() {
    }

    @Test
    @DisplayName("Test create candidate")
    void testCreateCandidate() {
        CandidateDTO newCandidate = new CandidateDTO();
        newCandidate.setName("Test Candidate");
        newCandidate.setPhone("+45 11 22 33 44");
        newCandidate.setEducation("Computer Science");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(newCandidate)
                .when()
                .post("/candidates")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Candidate"))
                .body("education", equalTo("Computer Science"));
    }

    @Test
    @DisplayName("Test create candidate with missing name")
    void testCreateCandidateValidation() {
        CandidateDTO invalidCandidate = new CandidateDTO();
        invalidCandidate.setPhone("+45 11 22 33 44");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(invalidCandidate)
                .when()
                .post("/candidates")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Test update candidate")
    void testUpdateCandidate() {
        CandidateDTO updateCandidate = new CandidateDTO();
        updateCandidate.setName("Updated Name");
        updateCandidate.setPhone("+45 99 88 77 66");
        updateCandidate.setEducation("Updated Education");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(updateCandidate)
                .when()
                .put("/candidates/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"));
    }

    @Test
    @DisplayName("Test delete candidate as user should fail")
    void testDeleteCandidateAsUser() {
    }

    @Test
    @DisplayName("Test delete candidate as admin")
    void testDeleteCandidateAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/candidates/3")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Test add skill to candidate")
    void testAddSkillToCandidate() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .put("/candidates/2/skills/1")
                .then()
                .statusCode(200)
                .body("skills.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Test get top candidate by popularity - calls real external API")
    void testGetTopCandidateByPopularity() {

    }
}
