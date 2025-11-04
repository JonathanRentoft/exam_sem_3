package app;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TripControllerTest extends IntegrationTestBase {

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
    @DisplayName("Test register new user")
    void testRegister() {
        UserDTO userDTO = new UserDTO("newuser", "password123");
        given()
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("username", equalTo("newuser"));
    }

    @Test
    @DisplayName("Test get all trips")
    void testGetAllTrips() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].name", notNullValue())
                .body("[0].category", notNullValue());
    }

    @Test
    @DisplayName("Test get all trips without authentication")
    void testGetAllTripsUnauthorized() {
        given()
                .when()
                .get("/trips")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Test get trips by category")
    void testGetTripsByCategory() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .queryParam("category", "beach")
                .when()
                .get("/trips")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].category", equalTo("beach"));
    }

    @Test
    @DisplayName("Test get trip by ID with packing items")
    void testGetTripById() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", notNullValue())
                .body("category", notNullValue())
                .body("guides", notNullValue())
                .body("packingItems", notNullValue());
    }

    @Test
    @DisplayName("Test get non-existent trip")
    void testGetTripByIdNotFound() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/999")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("Test create trip")
    void testCreateTrip() {
        TripDTO newTrip = new TripDTO();
        newTrip.setName("Test Trip");
        newTrip.setStartTime(ZonedDateTime.parse("2025-11-01T10:00:00Z"));
        newTrip.setEndTime(ZonedDateTime.parse("2025-11-08T18:00:00Z"));
        newTrip.setLocationCoordinates("50.0,10.0");
        newTrip.setPrice(10000.0);
        newTrip.setCategory("city");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(newTrip)
                .when()
                .post("/trips")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Trip"))
                .body("category", equalTo("city"))
                .body("price", equalTo(10000.0f));
    }

    @Test
    @DisplayName("Test create trip with missing required fields")
    void testCreateTripValidation() {
        TripDTO invalidTrip = new TripDTO();
        invalidTrip.setPrice(10000.0);

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(invalidTrip)
                .when()
                .post("/trips")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Test update trip")
    void testUpdateTrip() {
        TripDTO updateTrip = new TripDTO();
        updateTrip.setName("Updated Trip Name");
        updateTrip.setStartTime(ZonedDateTime.parse("2025-06-01T10:00:00Z"));
        updateTrip.setEndTime(ZonedDateTime.parse("2025-06-08T18:00:00Z"));
        updateTrip.setLocationCoordinates("21.1619,-86.8515");
        updateTrip.setPrice(13000.0);
        updateTrip.setCategory("beach");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(updateTrip)
                .when()
                .put("/trips/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Trip Name"))
                .body("price", equalTo(13000.0f));
    }

    @Test
    @DisplayName("Test delete trip as user should fail")
    void testDeleteTripAsUser() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/trips/1")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Test delete trip as admin")
    void testDeleteTripAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/trips/3")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Test add guide to trip")
    void testAddGuideToTrip() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .put("/trips/2/guides/3")
                .then()
                .statusCode(200)
                .body("guides.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Test get guides total price")
    void testGetGuidesTotalPrice() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].guideId", notNullValue())
                .body("[0].totalPrice", notNullValue());
    }

    @Test
    @DisplayName("Test get trip packing weight")
    void testGetTripPackingWeight() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/1/packing/weight")
                .then()
                .statusCode(200)
                .body("tripId", equalTo(1))
                .body("totalWeightInGrams", notNullValue());
    }
}
