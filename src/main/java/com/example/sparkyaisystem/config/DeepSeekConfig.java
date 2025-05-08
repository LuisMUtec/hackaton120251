package com.example.sparkyaisystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api.deepseek")
@Data
public class DeepSeekConfig {
    private String apiKey;
    private String endpoint = "https://api.deepseek.com/v1";
    private String model = "deepseek-v3-0324";
}
