package com.example.sparkyaisystem;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Sparky AI System.
 * This system serves as a centralized hub for managing access to various AI models.
 */
@SpringBootApplication
@EnableScheduling
public class SparkyAiSystemApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        
        // Database properties
        System.setProperty("JDBC_DATABASE_URL", dotenv.get("JDBC_DATABASE_URL"));
        System.setProperty("JDBC_DATABASE_USERNAME", dotenv.get("JDBC_DATABASE_USERNAME"));
        System.setProperty("JDBC_DATABASE_PASSWORD", dotenv.get("JDBC_DATABASE_PASSWORD"));
        
        // JWT properties
        System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

        // GitHub properties (add these if they're needed)
        System.setProperty("GITHUB_API_KEY", dotenv.get("GITHUB_API_KEY"));


        SpringApplication.run(SparkyAiSystemApplication.class, args);
    }
}