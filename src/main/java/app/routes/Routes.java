package app.routes;

import app.controllers.CandidateController;
import app.controllers.ReportController;
import app.controllers.SecurityController;
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

    public static void configureRoutes(Javalin app, CandidateController candidateController,
                                       ReportController reportController,
                                       SecurityController securityController,
                                       JwtUtil jwtUtil) {

        // jwt token validation happens here before routes are executed
        app.beforeMatched(ctx -> {
            Set<RouteRole> routeRoles = ctx.routeRoles();

            if (routeRoles.contains(Role.ANYONE)) {
                return;
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

        // login and register are open to everyone
        app.post("/api/login", securityController::login, Role.ANYONE);
        app.post("/api/register", securityController::register, Role.ANYONE);

        // candidate endpoints need authentication
        app.get("/api/candidates", candidateController::getAllCandidates, Role.USER);
        app.get("/api/candidates/{id}", candidateController::getCandidateById, Role.USER);
        app.post("/api/candidates", candidateController::createCandidate, Role.USER);
        app.put("/api/candidates/{id}", candidateController::updateCandidate, Role.USER);
        app.delete("/api/candidates/{id}", candidateController::deleteCandidate, Role.ADMIN);
        app.put("/api/candidates/{candidateId}/skills/{skillId}", candidateController::addSkillToCandidate, Role.USER);

        // report endpoint for analytics
        app.get("/api/reports/candidates/top-by-popularity", reportController::getTopCandidateByPopularity, Role.USER);
    }
}
