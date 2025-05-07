package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubModelsServiceTest {

    private GitHubModelsService service;
    private AIModel openAiModel;
    private AIModel metaModel;
    private AIModel genericModel;

    @BeforeEach
    void setUp() {
        service = new GitHubModelsService();

        openAiModel = new AIModel();
        openAiModel.setProvider("OpenAI");
        openAiModel.setName("GPT-4 Test");

        metaModel = new AIModel();
        metaModel.setProvider("Meta");
        metaModel.setName("70B Large");

        genericModel = new AIModel();
        genericModel.setProvider("Unknown");
        genericModel.setName("GenericModel");
    }

    @Test
    void processChatRequest_WithSystemPrompt_ShouldIncludeContext() {
        String prompt = "Explain AI.";
        String context = "System instructions";
        String response = service.processChatRequest(openAiModel, prompt, context);

        assertTrue(response.contains("Using context: " + context));
        assertTrue(response.contains("GPT-4:"));
    }

    @Test
    void processChatRequest_WithoutSystemPrompt_ShouldNotIncludeContext() {
        String prompt = "Tell me about machine learning.";
        String response = service.processChatRequest(openAiModel, prompt, null);

        assertFalse(response.startsWith("Using context:"));
        assertTrue(response.contains("GPT-4:"));
    }

    @Test
    void processCompletionRequest_GeneratesContinuationAndRespectsMaxTokens() {
        String prompt = "Hello world";
        // Set maxTokens small to trigger truncation logic
        String response = service.processCompletionRequest(openAiModel, prompt, 1, 0.5f);

        // Since maxTokens=1, output length should not exceed maxTokens*4 + ellipsis
        assertTrue(response.length() <= 4 + 3);
        assertTrue(response.endsWith("..."));
    }

    @Test
    void processMultimodalRequest_ValidImage_ShouldReturnAnalysis() throws IOException {
        String text = "Analyze this image.";
        byte[] content = new byte[]{1, 2, 3};
        MultipartFile file = new MockMultipartFile("image", "pic.png", "image/png", content);
        String response = service.processMultimodalRequest(openAiModel, text, file);

        assertTrue(response.contains("I've analyzed the image 'pic.png'"));
    }

    @Test
    void processMultimodalRequest_EmptyFile_ShouldThrowIOException() {
        MultipartFile emptyFile = new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);
        assertThrows(IOException.class, () ->
                service.processMultimodalRequest(openAiModel, "msg", emptyFile)
        );
    }

    @Test
    void processMultimodalRequest_NonImageFile_ShouldThrowIOException() {
        byte[] content = "text data".getBytes();
        MultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", content);
        assertThrows(IOException.class, () ->
                service.processMultimodalRequest(openAiModel, "msg", file)
        );
    }

    @Test
    void estimateTokenCount_ShouldBeWithinExpectedRange() {
        String text = "abcd"; //
        int count = service.estimateTokenCount(text);
        assertTrue(count >= 1 && count <= 3, "Token count should be between 1 and 3 for input length 4");
    }

    @Test
    void processChatRequest_MetaProvider_ShouldUseMetaResponse() {
        String prompt = "Hello";
        String response = service.processChatRequest(metaModel, prompt, "");
        assertTrue(response.contains("Llama AI:"));
    }

    @Test
    void processChatRequest_GenericProvider_ShouldUseFallback() {
        String prompt = "Hi there";
        String response = service.processChatRequest(genericModel, prompt, "");
        assertTrue(response.contains("AI Assistant:"));
    }
}
