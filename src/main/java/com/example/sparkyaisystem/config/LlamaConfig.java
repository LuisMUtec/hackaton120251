package com.example.sparkyaisystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api.llama")
@Data
public class LlamaConfig {
    private String apiKey;
    private String endpoint = "https://api.llama-api.com";
    private String model = "llama4-scout-17b-16e-instruct";
}