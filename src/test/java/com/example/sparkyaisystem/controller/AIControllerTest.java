package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.aimodel.AIModelResponse;
import com.example.sparkyaisystem.model.dto.request.*;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.UserRepository;
import com.example.sparkyaisystem.security.JwtTokenProvider;
import com.example.sparkyaisystem.service.AIModelService;
import com.example.sparkyaisystem.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIControllerTest {

    @Mock private RequestService requestService;
    @Mock private AIModelService aiModelService;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks private AIController aiController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(jwtTokenProvider.resolveToken(httpRequest)).thenReturn("token123");
        when(jwtTokenProvider.getUserId("token123")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void shouldReturnAvailableModels() {
        AIModelResponse model = AIModelResponse.builder()
                .id(1L)
                .name("gpt-4")
                .provider("OpenAI")
                .description("desc")
                .active(true)
                .build();
        when(aiModelService.getAvailableModelsForUser(mockUser)).thenReturn(List.of(model));

        ResponseEntity<List<AIModelResponse>> response = aiController.getAvailableModels(httpRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("gpt-4", response.getBody().get(0).getName());
    }

    @Test
    void shouldProcessChatRequest() {
        ChatRequest chatRequest = new ChatRequest();
        AIResponse expected = AIResponse.builder()
                .response("respuesta generada")
                .build();

        when(requestService.processChatRequest(mockUser, chatRequest)).thenReturn(expected);

        ResponseEntity<AIResponse> response = aiController.processChatRequest(httpRequest, chatRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("respuesta generada", response.getBody().getResponse());
    }

    @Test
    void shouldProcessCompletionRequest() {
        CompletionRequest completionRequest = new CompletionRequest();
        AIResponse expected = AIResponse.builder()
                .response("texto completado")
                .build();

        when(requestService.processCompletionRequest(mockUser, completionRequest)).thenReturn(expected);

        ResponseEntity<AIResponse> response = aiController.processCompletionRequest(httpRequest, completionRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("texto completado", response.getBody().getResponse());
    }

    @Test
    void shouldProcessMultimodalRequest() {
        MultimodalRequest multimodalRequest = new MultimodalRequest();
        // Simula una imagen o archivo
        multimodalRequest.setImageFile(new MockMultipartFile("image", "test.png", "image/png", new byte[0]));
        multimodalRequest.setMessage("Describe la imagen");

        AIResponse expected = AIResponse.builder()
                .response("respuesta multimodal")
                .build();

        when(requestService.processMultimodalRequest(mockUser, multimodalRequest)).thenReturn(expected);

        ResponseEntity<AIResponse> response = aiController.processMultimodalRequest(httpRequest, multimodalRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("respuesta multimodal", response.getBody().getResponse());
    }

    @Test
    void shouldReturnUserRequestHistory() {
        RequestResponse request1 = new RequestResponse();
        request1.setId(1L);

        when(requestService.getUserRequestHistory(mockUser)).thenReturn(List.of(request1));

        ResponseEntity<List<RequestResponse>> response = aiController.getRequestHistory(httpRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().get(0).getId());
    }
}
