package com.example.sparkyaisystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation (Swagger UI).
 */
@Configuration
@ConfigurationProperties(prefix = "api.openai")
@Data
public class OpenAIConfig {
    private String apiKey;
    private String endpoint = "https://api.openai.com/v1";
    private String model = "o4-mini";
}