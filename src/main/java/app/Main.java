package app;

import app.config.ApplicationConfig;
import io.javalin.Javalin;

public class Main {
    
    public static void main(String[] args) {
        Javalin app = ApplicationConfig.startServer();
        
        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Trip Planning API");
            ApplicationConfig.stopServer(app);
        }));
    }
}
