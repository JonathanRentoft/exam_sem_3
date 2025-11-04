package app.config;

import app.controllers.CandidateController;
import app.controllers.ReportController;
import app.controllers.SecurityController;
import app.dao.CandidateDAO;
import app.dao.SkillDAO;
import app.dao.UserDAO;
import app.exceptions.ApiException;
import app.exceptions.ExceptionHandler;
import app.routes.Routes;
import app.security.JwtUtil;
import app.services.SkillStatsApiClient;
import app.utils.Populator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig {

    private static final int DEFAULT_PORT = 7070;
    private static final String SECRET_KEY = getSecretKey();

    public static Javalin startServer(int port) {


        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        // putting sample data in database so there is something to work with
        Populator.populate(emf);

        // creating daos for database operations
        CandidateDAO candidateDAO = new CandidateDAO(emf);
        SkillDAO skillDAO = new SkillDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        // external api client and jwt util
        SkillStatsApiClient skillStatsApiClient = new SkillStatsApiClient();
        JwtUtil jwtUtil = new JwtUtil(SECRET_KEY);

        // setting up controllers that handle the requests
        CandidateController candidateController = new CandidateController(candidateDAO, skillStatsApiClient);
        ReportController reportController = new ReportController(candidateDAO, skillStatsApiClient);
        SecurityController securityController = new SecurityController(userDAO, jwtUtil);

        // setting up json serialization to handle dates correctly
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinJackson(objectMapper, true));
            config.bundledPlugins.enableRouteOverview("/routes", Routes.Role.ANYONE);
        }).start(port);

        Routes.configureRoutes(app, candidateController, reportController, securityController, jwtUtil);

        app.get("/", ctx -> {
            ctx.redirect("/routes");
        }, Routes.Role.ANYONE);

        // global error handling
        app.exception(ApiException.class, ExceptionHandler::handleApiException);
        app.exception(Exception.class, ExceptionHandler::handleGenericException);

        System.out.println("Route Overview: http://localhost:" + port + "/routes");
        System.out.println("API endpoints: http://localhost:" + port + "/api");

        return app;
    }
     //Starts the server on the default port
    public static Javalin startServer() {
        return startServer(DEFAULT_PORT);
    }

    // getting jwt secret from environment or using default for development
    private static String getSecretKey() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }
        System.out.println("WARNING: Using default JWT secret. Set JWT_SECRET environment variable in production!");
        return "your-secret-key-change-in-production-min-256-bits";
    }

}
