package app.exceptions;

import io.javalin.http.Context;

/**
 * Global exception handler for the application
 */
public class ExceptionHandler {

    /**
     * Handles ApiException and returns proper JSON error response
     */
    public static void handleApiException(ApiException e, Context ctx) {
        ctx.status(e.getCode()).json(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    /**
     * Handles generic exceptions and returns 500 error
     */
    public static void handleGenericException(Exception e, Context ctx) {
        e.printStackTrace();
        ctx.status(500).json(new ErrorResponse(500, "Internal server error: " + e.getMessage()));
    }

    /**
     * Error response structure
     */
    public static class ErrorResponse {
        public int code;
        public String message;

        public ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}