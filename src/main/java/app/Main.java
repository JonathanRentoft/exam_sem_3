package app;

import app.config.ApplicationConfig;
import io.javalin.Javalin;

public class Main {
    
    public static void main(String[] args) {
        Javalin app = ApplicationConfig.startServer();

    }
}