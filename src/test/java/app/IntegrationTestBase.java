package app;

import app.controllers.GuideController;
import app.controllers.SecurityController;
import app.controllers.TripController;
import app.dao.UserDAO;
import app.routes.Routes;
import app.security.JwtUtil;
import app.security.Roles;
import app.services.SkillStatsApiClient;
import app.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTestBase {

    protected static final String SECRET_KEY = "randomkey123randomkey123randomkey123randomkey123";
    protected Javalin app;
    protected EntityManagerFactory emf;
    protected String userToken;
    protected String adminToken;
    protected int testPort;

    protected void setupTest(int port) {
        this.testPort = port;
        RestAssured.baseURI = "http://localhost:" + port + "/api";
        
        // Setup in-memory H2 database
        setupH2Database();
        
        // Populate with test data
        populateTestData();

        // Initialize DAOs
        TripDAO tripDAO = new TripDAO(emf);
        GuideDAO guideDAO = new GuideDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        // Initialize services
        SkillStatsApiClient packingApiClient = new SkillStatsApiClient();
        JwtUtil jwtUtil = new JwtUtil(SECRET_KEY);

        // Initialize controllers
        TripController tripController = new TripController(tripDAO, packingApiClient);
        GuideController guideController = new GuideController(guideDAO);
        SecurityController securityController = new SecurityController(userDAO, jwtUtil);

        // Configure JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configure Javalin
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinJackson(objectMapper, true));
        }).start(port);

        Routes.configureRoutes(app, tripController, guideController, securityController, jwtUtil);

        // Add exception handlers
        app.exception(app.exceptions.ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode()).json(java.util.Map.of("code", e.getCode(), "message", e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(java.util.Map.of("code", 500, "message", "Internal server error: " + e.getMessage()));
        });

        // Get authentication tokens
        userToken = getAuthToken("user", "user123");
        adminToken = getAuthToken("admin", "admin123");
    }

    private void setupH2Database() {
        // Create H2 in-memory database configuration
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        
        // H2 specific properties
        java.util.Properties props = new java.util.Properties();
        props.put("hibernate.connection.driver_class", "org.h2.Driver");
        props.put("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.connection.password", "");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.current_session_context_class", "thread");
        
        configuration.setProperties(props);
        
        // Add entities
        configuration.addAnnotatedClass(app.entities.Trip.class);
        configuration.addAnnotatedClass(app.entities.Guide.class);
        configuration.addAnnotatedClass(app.entities.User.class);
        
        org.hibernate.service.ServiceRegistry serviceRegistry = 
            new org.hibernate.boot.registry.StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
        emf = configuration.buildSessionFactory(serviceRegistry)
            .unwrap(EntityManagerFactory.class);
    }

    private void populateTestData() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create users
            User admin = new User("admin", "admin123");
            admin.addRole(Roles.ADMIN);
            admin.addRole(Roles.USER);
            em.persist(admin);

            User user = new User("user", "user123");
            user.addRole(Roles.USER);
            em.persist(user);

            // Create guides
            Guide guide1 = new Guide();
            guide1.setName("John Smith");
            guide1.setEmail("john.smith@example.com");
            guide1.setPhone("+45 12 34 56 78");
            guide1.setYearsOfExperience(10);
            em.persist(guide1);

            Guide guide2 = new Guide();
            guide2.setName("Maria Garcia");
            guide2.setEmail("maria.garcia@example.com");
            guide2.setPhone("+45 23 45 67 89");
            guide2.setYearsOfExperience(5);
            em.persist(guide2);

            Guide guide3 = new Guide();
            guide3.setName("Lars Nielsen");
            guide3.setEmail("lars.nielsen@example.com");
            guide3.setPhone("+45 34 56 78 90");
            guide3.setYearsOfExperience(8);
            em.persist(guide3);

            // Create trips
            Trip trip1 = new Trip();
            trip1.setName("Beach Paradise in Cancun");
            trip1.setStartTime(ZonedDateTime.parse("2025-06-01T10:00:00Z"));
            trip1.setEndTime(ZonedDateTime.parse("2025-06-08T18:00:00Z"));
            trip1.setLocationCoordinates("21.1619,-86.8515");
            trip1.setPrice(12500.0);
            trip1.setCategory("beach");
            trip1.addGuide(guide1);
            em.persist(trip1);

            Trip trip2 = new Trip();
            trip2.setName("City Break in Barcelona");
            trip2.setStartTime(ZonedDateTime.parse("2025-07-10T09:00:00Z"));
            trip2.setEndTime(ZonedDateTime.parse("2025-07-15T20:00:00Z"));
            trip2.setLocationCoordinates("41.3874,2.1686");
            trip2.setPrice(8500.0);
            trip2.setCategory("city");
            trip2.addGuide(guide2);
            em.persist(trip2);

            Trip trip3 = new Trip();
            trip3.setName("Forest Adventure in Norway");
            trip3.setStartTime(ZonedDateTime.parse("2025-08-05T08:00:00Z"));
            trip3.setEndTime(ZonedDateTime.parse("2025-08-12T19:00:00Z"));
            trip3.setLocationCoordinates("60.4720,8.4689");
            trip3.setPrice(15000.0);
            trip3.setCategory("forest");
            trip3.addGuide(guide1);
            trip3.addGuide(guide3);
            em.persist(trip3);

            em.getTransaction().commit();
        }
    }

    protected String getAuthToken(String username, String password) {
        UserDTO userDTO = new UserDTO(username, password);
        TokenDTO response = given()
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);
        return response.getToken();
    }

    protected void teardownTest() {
        if (app != null) {
            try {
                app.stop();
                // Give it time to fully shutdown
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (emf != null) {
            try {
                emf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
