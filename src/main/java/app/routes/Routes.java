package app.routes;

import app.controllers.GuideController;
import app.controllers.SecurityController;
import app.controllers.TripController;
import app.exceptions.ExceptionHandler;
import app.security.JwtUtil;
import app.security.Roles;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.Javalin;
import io.javalin.security.RouteRole;

import java.util.Set;

public class Routes {

    // Define route roles
    public enum Role implements RouteRole {
        ANYONE,
        USER,
        ADMIN
    }

    public static void configureRoutes(Javalin app, TripController tripController,
                                        GuideController guideController,
                                        SecurityController securityController,
                                        JwtUtil jwtUtil) {

        // Configure access manager for JWT authentication
        app.beforeMatched(ctx -> {
            Set<RouteRole> routeRoles = ctx.routeRoles();
            
            if (routeRoles.contains(Role.ANYONE)) {
                return; // Public endpoints
            }

            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).json(new ExceptionHandler.ErrorResponse(401, "Missing or invalid authorization header")).skipRemainingHandlers();
                return;
            }

            String token = authHeader.substring(7);
            try {
                Set<String> roles = jwtUtil.extractRoles(token);
                
                if (routeRoles.contains(Role.ADMIN) && !roles.contains(Roles.ADMIN.name())) {
                    ctx.status(403).json(new ExceptionHandler.ErrorResponse(403, "Forbidden: Admin access required")).skipRemainingHandlers();
                    return;
                } else if (routeRoles.contains(Role.USER) && roles.isEmpty()) {
                    ctx.status(403).json(new ExceptionHandler.ErrorResponse(403, "Forbidden: User access required")).skipRemainingHandlers();
                    return;
                }
            } catch (JWTVerificationException e) {
                ctx.status(401).json(new ExceptionHandler.ErrorResponse(401, "Invalid or expired token")).skipRemainingHandlers();
                return;
            }
        });

        // Public endpoints
        app.post("/api/login", securityController::login, Role.ANYONE);
        app.post("/api/register", securityController::register, Role.ANYONE);

        // Trip endpoints (protected)
        app.get("/api/trips", tripController::getAllTrips, Role.USER);
        app.get("/api/trips/{id}", tripController::getTripById, Role.USER);
        app.post("/api/trips", tripController::createTrip, Role.USER);
        app.put("/api/trips/{id}", tripController::updateTrip, Role.USER);
        app.delete("/api/trips/{id}", tripController::deleteTrip, Role.ADMIN);
        app.put("/api/trips/{tripId}/guides/{guideId}", tripController::addGuideToTrip, Role.USER);
        app.get("/api/trips/guides/totalprice", tripController::getGuidesTotalPrice, Role.USER);
        app.get("/api/trips/{id}/packing/weight", tripController::getTripPackingWeight, Role.USER);

        // Guide endpoints (protected)
        app.get("/api/guides", guideController::getAllGuides, Role.USER);
        app.get("/api/guides/{id}", guideController::getGuideById, Role.USER);
        app.post("/api/guides", guideController::createGuide, Role.USER);
        app.put("/api/guides/{id}", guideController::updateGuide, Role.USER);
        app.delete("/api/guides/{id}", guideController::deleteGuide, Role.ADMIN);
    }
}
