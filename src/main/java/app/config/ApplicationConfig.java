package app.config;

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


        // Get EntityManagerFactory
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        // Populate database with sample data
        Populator.populate(emf);

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

        // Configure JSON serialization with ZonedDateTime support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Create and configure Javalin server
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinJackson(objectMapper, true));
            // Enable route overview - will automatically update when routes are registered
            config.bundledPlugins.enableRouteOverview("/routes", Routes.Role.ANYONE);
        }).start(port);

        // Configure routes with security
        Routes.configureRoutes(app, tripController, guideController, securityController, jwtUtil);
        
        // Add a simple root endpoint that redirects to route overview
        app.get("/", ctx -> {
            ctx.redirect("/routes");
        }, Routes.Role.ANYONE);

        // Add global exception handlers
        app.exception(ApiException.class, ExceptionHandler::handleApiException);
        app.exception(Exception.class, ExceptionHandler::handleGenericException);

        // Log startup
        System.out.println(" Trip Planning API started successfully!");
        System.out.println(" Server running on: http://localhost:" + port);
        System.out.println(" Route Overview: http://localhost:" + port + "/routes");
        System.out.println(" API endpoints: http://localhost:" + port + "/api");
        System.out.println(" Default users:");
        System.out.println("   - Admin: admin/admin123");
        System.out.println("   - User: user/user123");
        System.out.println();
        System.out.println(" NOTE: Use /routes endpoint to see all available endpoints with authentication requirements.");

        return app;
    }

    /**
     * Starts the server on the default port
     */
    public static Javalin startServer() {
        return startServer(DEFAULT_PORT);
    }

    /**
     * Stops the Javalin server
     */
    public static void stopServer(Javalin app) {
        if (app != null) {
            app.stop();
            System.out.println("✅ Server stopped successfully");
        }
    }

    /**
     * Gets the JWT secret key from environment variable or uses default
     * In production, ALWAYS use environment variable!
     */
    private static String getSecretKey() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }
        // Development fallback - DO NOT use in production!
        System.out.println("⚠️  WARNING: Using default JWT secret. Set JWT_SECRET environment variable in production!");
        return "your-secret-key-change-in-production-min-256-bits";
    }

}
