package com.example.sparkyaisystem.config;

import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import com.example.sparkyaisystem.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    @Profile("!test") // Don't run this in test profile
    public CommandLineRunner initDatabase(
            AIModelRepository aiModelRepository,
            UserRepository userRepository,
            AuthService authService) {
        return args -> {
            // Initialize AI models if none exist
            if (aiModelRepository.count() == 0) {
                log.info("Initializing AI models...");
                List<AIModel> models = Arrays.asList(
                    // OpenAI model
                    createModel("o4-mini", "OpenAI", "multimodal", "OpenAI's efficient multimodal model capable of processing both text and images"),
                    
                    // DeepSeek model
                    createModel("DeepSeek-V3-0324", "DeepSeek", "chat", "DeepSeek's advanced language model optimized for detailed understanding and reasoning"),
                    
                    // Meta/Llama model
                    createModel("Llama-4-Scout-17B-16E-Instruct", "Meta", "chat", "Meta's efficient instruction-following model with 17 billion parameters and 16 experts")
                );
                
                aiModelRepository.saveAll(models);
                log.info("AI models initialized successfully");
            }
            
            // Create default admin user if none exists
            if (userRepository.count() == 0) {
                log.info("Creating default admin user...");
                RegisterRequest adminRequest = new RegisterRequest();
                adminRequest.setFirstName("Admin");
                adminRequest.setLastName("User");
                adminRequest.setEmail("admin@sparky.com");
                adminRequest.setPassword("admin123");
                
                try {
                    authService.registerSparkyAdmin(adminRequest);
                    log.info("Default admin user created successfully");
                } catch (Exception e) {
                    log.error("Failed to create default admin user: {}", e.getMessage());
                }
            }
        };
    }
    
    private AIModel createModel(String name, String provider, String type, String description) {
        AIModel model = new AIModel();
        model.setName(name);
        model.setProvider(provider);
        model.setType(type);
        model.setDescription(description);
        model.setActive(true);
        return model;
    }
}