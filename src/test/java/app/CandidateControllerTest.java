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

    }

    @Test
    @DisplayName("Test login with invalid credentials")
    void testLoginFailure() {

    }

    @Test
    @DisplayName("Test get all candidates")
    void testGetAllCandidates() {

    }

    @Test
    @DisplayName("Test get all candidates without authentication")
    void testGetAllCandidatesUnauthorized() {

    }

    @Test
    @DisplayName("Test get candidates by skill category")
    void testGetCandidatesByCategory() {

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

    }

    @Test
    @DisplayName("Test create candidate with missing name")
    void testCreateCandidateValidation() {

    }

    @Test
    @DisplayName("Test update candidate")
    void testUpdateCandidate() {

    }

    @Test
    @DisplayName("Test delete candidate as user should fail")
    void testDeleteCandidateAsUser() {

    }

    @Test
    @DisplayName("Test delete candidate as admin")
    void testDeleteCandidateAsAdmin() {

    }

    @Test
    @DisplayName("Test add skill to candidate")
    void testAddSkillToCandidate() {

    }

    @Test
    @DisplayName("Test get top candidate by popularity - calls real external API")
    void testGetTopCandidateByPopularity() {
    }
}
