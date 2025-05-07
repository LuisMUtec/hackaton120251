package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.aimodel.AIModelResponse;
import com.example.sparkyaisystem.model.dto.request.AIResponse;
import com.example.sparkyaisystem.model.dto.request.ChatRequest;
import com.example.sparkyaisystem.model.dto.request.CompletionRequest;
import com.example.sparkyaisystem.model.dto.request.MultimodalRequest;
import com.example.sparkyaisystem.model.dto.request.RequestResponse;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.UserRepository;
import com.example.sparkyaisystem.security.JwtTokenProvider;
import com.example.sparkyaisystem.service.AIModelService;
import com.example.sparkyaisystem.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyRole('USER', 'COMPANY_ADMIN', 'SPARKY_ADMIN')")
public class AIController {

    private final RequestService requestService;
    private final AIModelService aiModelService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AIController(RequestService requestService,
                        AIModelService aiModelService,
                        UserRepository userRepository,
                        JwtTokenProvider jwtTokenProvider) {
        this.requestService = requestService;
        this.aiModelService = aiModelService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Helper method to get user from JWT token
    private User getUserFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long userId = jwtTokenProvider.getUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/models")
    public ResponseEntity<List<AIModelResponse>> getAvailableModels(HttpServletRequest request) {
        User user = getUserFromToken(request);
        List<AIModelResponse> models = aiModelService.getAvailableModelsForUser(user);
        return ResponseEntity.ok(models);
    }

    @PostMapping("/chat")
    public ResponseEntity<AIResponse> processChatRequest(
            HttpServletRequest request,
            @Valid @RequestBody ChatRequest chatRequest) {
        User user = getUserFromToken(request);
        AIResponse response = requestService.processChatRequest(user, chatRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/completion")
    public ResponseEntity<AIResponse> processCompletionRequest(
            HttpServletRequest request,
            @Valid @RequestBody CompletionRequest completionRequest) {
        User user = getUserFromToken(request);
        AIResponse response = requestService.processCompletionRequest(user, completionRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/multimodal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AIResponse> processMultimodalRequest(
            HttpServletRequest request,
            @Valid @ModelAttribute MultimodalRequest multimodalRequest) {
        User user = getUserFromToken(request);
        AIResponse response = requestService.processMultimodalRequest(user, multimodalRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<RequestResponse>> getRequestHistory(HttpServletRequest request) {
        User user = getUserFromToken(request);
        List<RequestResponse> history = requestService.getUserRequestHistory(user);
        return ResponseEntity.ok(history);
    }
}