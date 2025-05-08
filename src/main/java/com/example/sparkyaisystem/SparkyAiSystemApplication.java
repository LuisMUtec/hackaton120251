package com.example.sparkyaisystem;

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
        SpringApplication.run(SparkyAiSystemApplication.class, args);
    }
}
