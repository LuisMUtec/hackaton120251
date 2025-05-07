package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.exception.LimitExceededException;
import com.example.sparkyaisystem.model.dto.request.AIResponse;
import com.example.sparkyaisystem.model.dto.request.ChatRequest;
import com.example.sparkyaisystem.model.dto.request.CompletionRequest;
import com.example.sparkyaisystem.model.dto.request.MultimodalRequest;
import com.example.sparkyaisystem.model.dto.request.RequestResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Request;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final AIModelRepository aiModelRepository;
    private final LimitRepository limitRepository;
    private final LimitService limitService;
    private final AIModelService aiModelService;
    private final GitHubModelsService gitHubModelsService;

    public RequestService(RequestRepository requestRepository,
                          AIModelRepository aiModelRepository,
                          LimitRepository limitRepository,
                          LimitService limitService,
                          AIModelService aiModelService,
                          GitHubModelsService gitHubModelsService) {
        this.requestRepository = requestRepository;
        this.aiModelRepository = aiModelRepository;
        this.limitRepository = limitRepository;
        this.limitService = limitService;
        this.aiModelService = aiModelService;
        this.gitHubModelsService = gitHubModelsService;
    }

    @Transactional
    public AIResponse processChatRequest(User user, ChatRequest chatRequest) {
        log.info("Processing chat request for user: {}, model: {}", user.getEmail(), chatRequest.getModelId());
        AIModel model = aiModelRepository.findById(chatRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if model is available for user
        if (!aiModelService.isModelAvailableForUser(user, model)) {
            log.warn("Model {} is not available for user {}", model.getName(), user.getEmail());
            throw new RuntimeException("Model is not available for this user");
        }

        // Estimate tokens to be consumed
        int estimatedTokens = gitHubModelsService.estimateTokenCount(chatRequest.getMessage());
        log.debug("Estimated tokens for chat request: {}", estimatedTokens);

        // Check if user has enough limit and update it
        // This will throw LimitExceededException if limit is exceeded
        try {
            limitService.checkAndUpdateLimit(user, model, estimatedTokens);
        } catch (LimitExceededException e) {
            log.warn("User {} has exceeded their limit for model {}: {}", user.getEmail(), model.getName(), e.getMessage());
            throw e; // Re-throw the exception to be handled by the global exception handler
        }

        // Create request record
        Request request = new Request();
        request.setUser(user);
        request.setModel(model);
        request.setQuery(chatRequest.getMessage());
        request.setRequestTime(LocalDateTime.now());

        try {
            // Call GitHub Models service
            log.debug("Calling GitHub Models service for chat request");
            String response = gitHubModelsService.processChatRequest(model, chatRequest.getMessage(), chatRequest.getSystemPrompt());
            int actualTokens = estimatedTokens; // In a real app, this would be more accurately calculated

            // Update request with response
            request.setResponse(response);
            request.setSuccessful(true);
            request.setTokensConsumed(actualTokens);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);
            log.info("Chat request processed successfully, request ID: {}", request.getId());

            // Get user's limit for this model
            Limit limit = limitRepository.findByUserAndModel(user, model)
                    .orElseThrow(() -> new RuntimeException("User does not have a limit for this model"));

            // Build response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .response(response)
                    .tokensConsumed(actualTokens)
                    .successful(true)
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .limitStatus(buildLimitStatus(limit))
                    .build();

        } catch (Exception e) {
            // Handle error
            log.error("Error processing chat request: {}", e.getMessage(), e);
            request.setSuccessful(false);
            request.setErrorMessage(e.getMessage());
            request.setTokensConsumed(0);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);

            // Return error response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .successful(false)
                    .errorMessage(e.getMessage())
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .build();
        }
    }

    @Transactional
    public AIResponse processCompletionRequest(User user, CompletionRequest completionRequest) {
        log.info("Processing completion request for user: {}, model: {}", user.getEmail(), completionRequest.getModelId());
        AIModel model = aiModelRepository.findById(completionRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if model is available for user
        if (!aiModelService.isModelAvailableForUser(user, model)) {
            log.warn("Model {} is not available for user {}", model.getName(), user.getEmail());
            throw new RuntimeException("Model is not available for this user");
        }

        // Estimate tokens to be consumed
        int estimatedTokens = gitHubModelsService.estimateTokenCount(completionRequest.getPrompt());
        log.debug("Estimated tokens for completion request: {}", estimatedTokens);

        // Check if user has enough limit and update it
        // This will throw LimitExceededException if limit is exceeded
        try {
            limitService.checkAndUpdateLimit(user, model, estimatedTokens);
        } catch (LimitExceededException e) {
            log.warn("User {} has exceeded their limit for model {}: {}", user.getEmail(), model.getName(), e.getMessage());
            throw e; // Re-throw the exception to be handled by the global exception handler
        }

        // Create request record
        Request request = new Request();
        request.setUser(user);
        request.setModel(model);
        request.setQuery(completionRequest.getPrompt());
        request.setRequestTime(LocalDateTime.now());

        try {
            // Call GitHub Models service
            log.debug("Calling GitHub Models service for completion request");
            String response = gitHubModelsService.processCompletionRequest(
                    model, 
                    completionRequest.getPrompt(), 
                    completionRequest.getMaxTokens(), 
                    completionRequest.getTemperature());
            int actualTokens = estimatedTokens; // In a real app, this would be more accurately calculated

            // Update request with response
            request.setResponse(response);
            request.setSuccessful(true);
            request.setTokensConsumed(actualTokens);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);
            log.info("Completion request processed successfully, request ID: {}", request.getId());

            // Get user's limit for this model
            Limit limit = limitRepository.findByUserAndModel(user, model)
                    .orElseThrow(() -> new RuntimeException("User does not have a limit for this model"));

            // Build response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .response(response)
                    .tokensConsumed(actualTokens)
                    .successful(true)
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .limitStatus(buildLimitStatus(limit))
                    .build();

        } catch (Exception e) {
            // Handle error
            log.error("Error processing completion request: {}", e.getMessage(), e);
            request.setSuccessful(false);
            request.setErrorMessage(e.getMessage());
            request.setTokensConsumed(0);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);

            // Return error response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .successful(false)
                    .errorMessage(e.getMessage())
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .build();
        }
    }

    @Transactional
    public AIResponse processMultimodalRequest(User user, MultimodalRequest multimodalRequest) {
        log.info("Processing multimodal request for user: {}, model: {}", user.getEmail(), multimodalRequest.getModelId());
        AIModel model = aiModelRepository.findById(multimodalRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if model is available for user
        if (!aiModelService.isModelAvailableForUser(user, model)) {
            log.warn("Model {} is not available for user {}", model.getName(), user.getEmail());
            throw new RuntimeException("Model is not available for this user");
        }

        // Check if model type is multimodal
        if (!"multimodal".equalsIgnoreCase(model.getType())) {
            log.warn("Model {} does not support multimodal requests", model.getName());
            throw new RuntimeException("Selected model does not support multimodal requests");
        }

        // Estimate tokens to be consumed (multimodal requests typically consume more tokens)
        int estimatedTokens = gitHubModelsService.estimateTokenCount(multimodalRequest.getMessage()) * 2;
        log.debug("Estimated tokens for multimodal request: {}", estimatedTokens);

        // Check if user has enough limit and update it
        // This will throw LimitExceededException if limit is exceeded
        try {
            limitService.checkAndUpdateLimit(user, model, estimatedTokens);
        } catch (LimitExceededException e) {
            log.warn("User {} has exceeded their limit for model {}: {}", user.getEmail(), model.getName(), e.getMessage());
            throw e; // Re-throw the exception to be handled by the global exception handler
        }

        // Get file name
        MultipartFile imageFile = multimodalRequest.getImageFile();
        String fileName = imageFile.getOriginalFilename();
        log.debug("Processing multimodal request with image: {}", fileName);

        // Create request record
        Request request = new Request();
        request.setUser(user);
        request.setModel(model);
        request.setQuery(multimodalRequest.getMessage());
        request.setFileName(fileName);
        request.setRequestTime(LocalDateTime.now());

        try {
            // Call GitHub Models service
            log.debug("Calling GitHub Models service for multimodal request");
            String response = gitHubModelsService.processMultimodalRequest(model, multimodalRequest.getMessage(), imageFile);
            int actualTokens = estimatedTokens; // In a real app, this would be more accurately calculated

            // Update request with response
            request.setResponse(response);
            request.setSuccessful(true);
            request.setTokensConsumed(actualTokens);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);
            log.info("Multimodal request processed successfully, request ID: {}", request.getId());

            // Get user's limit for this model
            Limit limit = limitRepository.findByUserAndModel(user, model)
                    .orElseThrow(() -> new RuntimeException("User does not have a limit for this model"));

            // Build response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .response(response)
                    .tokensConsumed(actualTokens)
                    .successful(true)
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .limitStatus(buildLimitStatus(limit))
                    .build();

        } catch (Exception e) {
            // Handle error
            log.error("Error processing multimodal request: {}", e.getMessage(), e);
            request.setSuccessful(false);
            request.setErrorMessage(e.getMessage());
            request.setTokensConsumed(0);
            request.setResponseTime(LocalDateTime.now());

            // Save request
            request = requestRepository.save(request);

            // Return error response
            return AIResponse.builder()
                    .requestId(request.getId())
                    .modelName(model.getName())
                    .modelProvider(model.getProvider())
                    .successful(false)
                    .errorMessage(e.getMessage())
                    .processingTimeMs(ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()))
                    .build();
        }
    }

    public List<RequestResponse> getUserRequestHistory(User user) {
        List<Request> requests = requestRepository.findByUser(user);
        return requests.stream()
                .map(this::mapToRequestResponse)
                .collect(Collectors.toList());
    }

    private RequestResponse mapToRequestResponse(Request request) {
        return RequestResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userName(request.getUser().getFirstName() + " " + request.getUser().getLastName())
                .modelId(request.getModel().getId())
                .modelName(request.getModel().getName())
                .modelProvider(request.getModel().getProvider())
                .modelType(request.getModel().getType())
                .query(request.getQuery())
                .response(request.getResponse())
                .errorMessage(request.getErrorMessage())
                .successful(request.isSuccessful())
                .tokensConsumed(request.getTokensConsumed())
                .fileName(request.getFileName())
                .requestTime(request.getRequestTime())
                .responseTime(request.getResponseTime())
                .processingTimeMs(request.getResponseTime() != null ? 
                        ChronoUnit.MILLIS.between(request.getRequestTime(), request.getResponseTime()) : null)
                .build();
    }

    private AIResponse.LimitStatus buildLimitStatus(Limit limit) {
        return AIResponse.LimitStatus.builder()
                .usedRequests(limit.getUsedRequests())
                .maxRequests(limit.getMaxRequestsPerWindow())
                .usedTokens(limit.getUsedTokens())
                .maxTokens(limit.getMaxTokensPerWindow())
                .windowType(limit.getWindowType())
                .windowEndsAt(limit.getWindowEndTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

}
