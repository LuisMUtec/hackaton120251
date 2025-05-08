package com.example.sparkyaisystem.config;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubModelsConfig {
    
    @Value("${GITHUB_API_KEY}")
    private String githubToken;
    
    @Value("${github.models.endpoint:https://models.github.ai/inference}")
    private String endpoint;
    
    @Bean
    public ChatCompletionsClient chatCompletionsClient() {
        return new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(githubToken))
                .endpoint(endpoint)
                .buildClient();
    }
}