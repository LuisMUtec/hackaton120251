package com.example.sparkyaisystem.service;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.models.*;
import com.example.sparkyaisystem.model.entity.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Service for interacting with GitHub Models SDK.
 * This implementation uses the Azure Inference SDK to interact with GitHub's AI model inference endpoint.
 */
@Service
@Slf4j
public class GitHubModelsService {

    @Autowired
    private ChatCompletionsClient chatCompletionsClient;
    
    @Value("${github.models.deepseek}")
    private String deepseekModel;
    
    @Value("${github.models.openai}")
    private String openaiModel;
    
    @Value("${github.models.llama}")
    private String llamaModel;
    
    private final Random random = new Random();

    /**
     * Process a chat request with the specified model.
     *
     * @param model The AI model to use
     * @param message The user's message
     * @param systemPrompt Optional system prompt to guide the model
     * @return The model's response
     */
    public String processChatRequest(AIModel model, String message, String systemPrompt) {
        log.info("Processing chat request with model: {}, message length: {}", model.getName(), message.length());
        
        try {
            // Determine which GitHub model to use based on provided AIModel
            String githubModel = mapToGitHubModel(model);
            
            // Create chat messages
            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            
            // Add system prompt if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                chatMessages.add(new ChatRequestSystemMessage(systemPrompt));
            } else {
                chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
            }
            
            // Add user message
            chatMessages.add(new ChatRequestUserMessage(message));
            
            // Create chat completion options
            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setModel(githubModel);
            
            // Make API call to GitHub Models
            log.debug("Sending request to GitHub Models SDK with model: {}", githubModel);
            ChatCompletions completions = chatCompletionsClient.complete(options);
            
            // Extract and return the response
            String response = completions.getChoice().getMessage().getContent();
            log.info("Successfully received response from GitHub Models SDK");
            
            return response;
        } catch (Exception e) {
            log.error("Error processing chat request with GitHub Models SDK: {}", e.getMessage(), e);
            return "Error processing request: " + e.getMessage();
        }
    }

    /**
     * Process a completion request with the specified model.
     *
     * @param model The AI model to use
     * @param prompt The prompt for text completion
     * @param maxTokens Maximum tokens to generate
     * @param temperature Controls randomness (0-1)
     * @return The model's response
     */
    public String processCompletionRequest(AIModel model, String prompt, Integer maxTokens, Float temperature) {
        log.info("Processing completion request with model: {}, prompt length: {}", model.getName(), prompt.length());
        
        try {
            // For completion requests, we'll use the chat API with just the user message
            String githubModel = mapToGitHubModel(model);
            
            // Create chat messages (for completion, we just send the prompt as a user message)
            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestUserMessage(prompt));
            
            // Create chat completion options
            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setModel(githubModel);
            
            // Set max tokens if provided
            if (maxTokens != null) {
                options.setMaxTokens(maxTokens);
            }
            
            // Set temperature if provided
            if (temperature != null) {
                options.setTemperature(Double.valueOf(temperature));
            }
            
            // Make API call to GitHub Models
            log.debug("Sending completion request to GitHub Models SDK with model: {}", githubModel);
            ChatCompletions completions = chatCompletionsClient.complete(options);
            
            // Extract and return the response
            String response = completions.getChoice().getMessage().getContent();
            log.info("Successfully received completion response from GitHub Models SDK");
            
            return response;
        } catch (Exception e) {
            log.error("Error processing completion request with GitHub Models SDK: {}", e.getMessage(), e);
            return "Error processing completion request: " + e.getMessage();
        }
    }

    /**
     * Process a multimodal request with the specified model.
     *
     * @param model The AI model to use
     * @param message The user's message
     * @param imageFile The image file
     * @return The model's response
     * @throws IOException If there's an error processing the image
     */
    public String processMultimodalRequest(AIModel model, String message, MultipartFile imageFile) throws IOException {
        log.info("Processing multimodal request with model: {}, message length: {}, image: {}", 
                model.getName(), message.length(), imageFile.getOriginalFilename());
        
        // Validate image file
        validateImageFile(imageFile);
        
        try {
            // Only OpenAI o4-mini supports multimodal in our setup
            if (model.getProvider().equalsIgnoreCase("OpenAI") && model.getName().equalsIgnoreCase("o4-mini")) {
                // Get the model reference for GitHub Models SDK
                String githubModel = openaiModel;
                
                // Encode image to base64
                byte[] imageBytes = imageFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String dataUri = "data:image/" + getImageType(imageFile) + ";base64," + base64Image;
                
                // Create message content with both text and image
                List<ChatMessageContentItem> contentItems = new ArrayList<>();
                contentItems.add(new ChatMessageTextContentItem(message));
                
                // Create image content item with data URI
                ChatMessageImageUrl imageUrl = new ChatMessageImageUrl(dataUri);
                contentItems.add(new ChatMessageImageContentItem(imageUrl));
                
                // Create user message with content
                ChatRequestUserMessage userMessage = new ChatRequestUserMessage(contentItems.toString());
                
                // Create chat messages
                List<ChatRequestMessage> chatMessages = new ArrayList<>();
                chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant that can analyze images."));
                chatMessages.add(userMessage);
                
                // Create chat completion options
                ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
                options.setModel(githubModel);
                
                // Make API call to GitHub Models
                log.debug("Sending multimodal request to GitHub Models SDK with model: {}", githubModel);
                ChatCompletions completions = chatCompletionsClient.complete(options);
                
                // Extract and return the response
                String response = completions.getChoice().getMessage().getContent();
                log.info("Successfully received multimodal response from GitHub Models SDK");
                
                return response;
            } else {
                return "Multimodal requests are currently only supported for OpenAI o4-mini model";
            }
        } catch (Exception e) {
            log.error("Error processing multimodal request with GitHub Models SDK: {}", e.getMessage(), e);
            return "Error processing multimodal request: " + e.getMessage();
        }
    }

    /**
     * Estimate the number of tokens that will be consumed by a request.
     *
     * @param text The input text
     * @return Estimated token count
     */
    public int estimateTokenCount(String text) {
        // A simple estimation: roughly 4 characters per token
        int baseEstimate = text.length() / 4 + 1;
        
        // Add some randomness to make it more realistic
        int variance = Math.max(1, baseEstimate / 10);
        return baseEstimate + random.nextInt(variance * 2) - variance;
    }

    // Helper methods
    
    /**
     * Maps our internal AIModel to the appropriate GitHub Models SDK model identifier.
     *
     * @param model Our internal AIModel
     * @return The GitHub Models SDK model identifier
     */
    private String mapToGitHubModel(AIModel model) {
        String provider = model.getProvider().toLowerCase();
        String modelName = model.getName().toLowerCase();
        
        if (provider.contains("deepseek") || modelName.contains("deepseek")) {
            return deepseekModel;
        } else if (provider.contains("openai") || modelName.contains("o4")) {
            return openaiModel;
        } else if (provider.contains("meta") || provider.contains("llama") || 
                  modelName.contains("llama") || modelName.contains("scout")) {
            return llamaModel;
        } else {
            // Default to DeepSeek if we can't determine the model
            log.warn("Could not determine GitHub model for {}/{}. Defaulting to DeepSeek.", 
                    model.getProvider(), model.getName());
            return deepseekModel;
        }
    }
    
    private String getImageType(MultipartFile imageFile) {
        String contentType = imageFile.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            return contentType.substring(6); // Return image type (jpeg, png, etc.)
        }
        return "jpeg"; // Default to jpeg if content type is not available
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IOException("Image file is empty");
        }
        
        // Check file size (e.g., limit to 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("Image file size exceeds the maximum limit of 10MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            throw new IOException("File must be an image");
        }
    }
}