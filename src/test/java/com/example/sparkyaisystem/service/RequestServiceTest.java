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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock private RequestRepository requestRepository;
    @Mock private AIModelRepository aiModelRepository;
    @Mock private LimitRepository limitRepository;
    @Mock private LimitService limitService;
    @Mock private AIModelService aiModelService;
    @Mock private GitHubModelsService gitHubModelsService;

    @InjectMocks private RequestService requestService;

    private User user;
    private AIModel model;
    private Limit limit;
    private Request savedRequest;
    private final int estimatedTokens = 4;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        model = new AIModel();
        model.setId(2L);
        model.setName("TestModel");
        model.setType("chat");
        model.setProvider("OpenAI");

        limit = new Limit();
        limit.setUser(user);
        limit.setModel(model);
        limit.setMaxRequestsPerWindow(10);
        limit.setMaxTokensPerWindow(100);
        limit.setUsedRequests(0);
        limit.setUsedTokens(0);
        limit.setWindowType("daily");
        limit.setWindowStartTime(LocalDateTime.now().minusMinutes(10));
        limit.setWindowEndTime(LocalDateTime.now().plusHours(14));

        savedRequest = new Request();
        savedRequest.setId(5L);
        savedRequest.setUser(user);
        savedRequest.setModel(model);
    }

    @Test
    void processChatRequestSuccess() {
        // --- Arrange ---
        ChatRequest chatReq = new ChatRequest();
        chatReq.setModelId(model.getId());
        chatReq.setMessage("Hello World");
        chatReq.setSystemPrompt("ctx");

        when(aiModelRepository.findById(eq(model.getId())))
                .thenReturn(Optional.of(model));
        when(aiModelService.isModelAvailableForUser(user, model))
                .thenReturn(true);
        when(gitHubModelsService.estimateTokenCount(chatReq.getMessage()))
                .thenReturn(4);

        // Aquí simulamos que checkAndUpdateLimit *incrementa* el objeto limit
        doAnswer(invocation -> {
            limit.setUsedRequests(limit.getUsedRequests() + 1);
            limit.setUsedTokens(limit.getUsedTokens() + 4);
            return null;
        }).when(limitService).checkAndUpdateLimit(user, model, 4);

        // Simulamos el guardado para asignar un ID
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request req = inv.getArgument(0);
            req.setId(savedRequest.getId());
            return req;
        });

        when(limitRepository.findByUserAndModel(user, model))
                .thenReturn(Optional.of(limit));
        when(gitHubModelsService.processChatRequest(
                eq(model), anyString(), anyString()))
                .thenReturn("response text");

        int initialRequests = limit.getUsedRequests();
        int initialTokens   = limit.getUsedTokens();

        // --- Act ---
        AIResponse resp = requestService.processChatRequest(user, chatReq);

        // --- Assert ---
        assertTrue(resp.isSuccessful());
        assertEquals(savedRequest.getId(), resp.getRequestId());
        assertEquals("response text", resp.getResponse());
        assertEquals(4, resp.getTokensConsumed());

        // Ahora sí esperamos que el stub haya incrementado los contadores
        assertEquals(initialRequests + 1, limit.getUsedRequests());
        assertEquals(initialTokens   + 4, limit.getUsedTokens());

        // Y que esos valores estén reflejados en el response
        assertEquals(limit.getUsedRequests(), resp.getLimitStatus().getUsedRequests());
        assertEquals(limit.getUsedTokens(),   resp.getLimitStatus().getUsedTokens());

        // Verificaciones de interacciones
        verify(aiModelRepository).findById(model.getId());
        verify(aiModelService).isModelAvailableForUser(user, model);
        verify(gitHubModelsService).estimateTokenCount("Hello World");
        verify(limitService).checkAndUpdateLimit(user, model, 4);
        verify(requestRepository).save(any(Request.class));
    }


    @Test
    void processChatRequestModelUnavailableThrows() {
        // Arrange
        ChatRequest chatReq = new ChatRequest();
        chatReq.setModelId(model.getId());
        chatReq.setMessage("Hi");

        when(aiModelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        when(aiModelService.isModelAvailableForUser(user, model)).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> requestService.processChatRequest(user, chatReq));
        assertEquals("Model is not available for this user", ex.getMessage());
    }

    @Test
    void processChatRequestLimitExceededReThrows() {
        // Arrange
        ChatRequest chatReq = new ChatRequest();
        chatReq.setModelId(model.getId());
        chatReq.setMessage("msg");

        when(aiModelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        when(aiModelService.isModelAvailableForUser(user, model)).thenReturn(true);
        when(gitHubModelsService.estimateTokenCount(anyString())).thenReturn(1);
        doThrow(new LimitExceededException("requests",0,0,"daily",LocalDateTime.now()))
                .when(limitService).checkAndUpdateLimit(user, model, 1);

        // Act & Assert
        assertThrows(LimitExceededException.class,
                () -> requestService.processChatRequest(user, chatReq));
    }

    @Test
    void processCompletionRequestSuccess() {
        // Arrange
        CompletionRequest compReq = new CompletionRequest();
        compReq.setModelId(model.getId());
        compReq.setPrompt("Prompt");
        compReq.setMaxTokens(10);
        compReq.setTemperature(0.7f);

        when(aiModelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        when(aiModelService.isModelAvailableForUser(user, model)).thenReturn(true);
        when(gitHubModelsService.estimateTokenCount(compReq.getPrompt())).thenReturn(5);
        doNothing().when(limitService).checkAndUpdateLimit(user, model, 5);
        when(gitHubModelsService.processCompletionRequest(model, compReq.getPrompt(), compReq.getMaxTokens(), compReq.getTemperature()))
                .thenReturn("completed");
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            r.setId(7L);
            return r;
        });
        when(limitRepository.findByUserAndModel(user, model)).thenReturn(Optional.of(limit));

        // Act
        AIResponse resp = requestService.processCompletionRequest(user, compReq);

        // Assert
        assertTrue(resp.isSuccessful());
        assertEquals(7L, resp.getRequestId());
        assertEquals("completed", resp.getResponse());
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void processMultimodalRequestUnsupportedTypeThrows() {
        // Arrange
        model.setType("chat");
        MultimodalRequest mmReq = new MultimodalRequest();
        mmReq.setModelId(model.getId());
        mmReq.setMessage("msg");
        MultipartFile file = new MockMultipartFile("img","file.png","image/png",new byte[]{1});
        mmReq.setImageFile(file);

        when(aiModelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        when(aiModelService.isModelAvailableForUser(user, model)).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> requestService.processMultimodalRequest(user, mmReq));
        assertEquals("Selected model does not support multimodal requests", ex.getMessage());
    }

    @Test
    void getUserRequestHistoryReturnsMappedResponses() {
        // Arrange
        Request r = new Request();
        r.setId(20L);
        r.setUser(user);
        r.setModel(model);
        when(requestRepository.findByUser(user)).thenReturn(List.of(r));

        // Act
        List<RequestResponse> history = requestService.getUserRequestHistory(user);

        // Assert
        assertEquals(1, history.size());
        assertEquals(r.getId(), history.get(0).getId());
    }
}
