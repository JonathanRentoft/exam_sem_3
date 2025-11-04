package app;

import app.config.HibernateConfig;
import app.controllers.CandidateController;
import app.controllers.ReportController;
import app.controllers.SecurityController;
import app.dao.CandidateDAO;
import app.dao.SkillDAO;
import app.dao.UserDAO;
import app.dto.TokenDTO;
import app.dto.UserDTO;
import app.routes.Routes;
import app.security.JwtUtil;
import app.security.Roles;
import app.services.SkillStatsApiClient;
import app.entities.User;
import app.entities.Candidate;
import app.entities.Skill;
import app.entities.SkillCategory;
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

        setupH2Database();
        populateTestData();

        // setting up daos for database access
        CandidateDAO candidateDAO = new CandidateDAO(emf);
        SkillDAO skillDAO = new SkillDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        SkillStatsApiClient skillStatsApiClient = new SkillStatsApiClient();
        JwtUtil jwtUtil = new JwtUtil(SECRET_KEY);

        CandidateController candidateController = new CandidateController(candidateDAO, skillStatsApiClient);
        ReportController reportController = new ReportController(candidateDAO, skillStatsApiClient);
        SecurityController securityController = new SecurityController(userDAO, jwtUtil);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinJackson(objectMapper, true));
        }).start(port);

        Routes.configureRoutes(app, candidateController, reportController, securityController, jwtUtil);

        app.exception(app.exceptions.ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode()).json(java.util.Map.of("code", e.getCode(), "message", e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(java.util.Map.of("code", 500, "message", "Internal server error: " + e.getMessage()));
        });

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

        // registering entities with hibernate
        configuration.addAnnotatedClass(app.entities.Candidate.class);
        configuration.addAnnotatedClass(app.entities.Skill.class);
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

            User admin = new User("admin", "admin123");
            admin.addRole(Roles.ADMIN);
            admin.addRole(Roles.USER);
            em.persist(admin);

            User user = new User("user", "user123");
            user.addRole(Roles.USER);
            em.persist(user);

            // creating skills before candidates because of the relationship
            Skill java = new Skill("Java", "java", SkillCategory.PROG_LANG, "Programming language");
            em.persist(java);

            Skill python = new Skill("Python", "python", SkillCategory.PROG_LANG, "Programming language");
            em.persist(python);

            Skill springBoot = new Skill("Spring Boot", "spring-boot", SkillCategory.FRAMEWORK, "Java framework");
            em.persist(springBoot);

            Skill postgresql = new Skill("PostgreSQL", "postgresql", SkillCategory.DB, "Database");
            em.persist(postgresql);

            Candidate candidate1 = new Candidate("John Nielsen", "+45 12 34 56 78", "Computer Science BSc");
            candidate1.addSkill(java);
            candidate1.addSkill(springBoot);
            em.persist(candidate1);

            Candidate candidate2 = new Candidate("Maria Hansen", "+45 23 45 67 89", "Software Engineering MSc");
            candidate2.addSkill(python);
            em.persist(candidate2);

            Candidate candidate3 = new Candidate("Lars Andersen", "+45 34 56 78 90", "Datamatiker");
            candidate3.addSkill(java);
            candidate3.addSkill(postgresql);
            em.persist(candidate3);

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
