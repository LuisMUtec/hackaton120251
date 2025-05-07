package com.example.sparkyaisystem.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private Long requestId;
    private String modelName;
    private String modelProvider;
    private String response;
    private int tokensConsumed;
    private boolean successful;
    private String errorMessage;
    private Long processingTimeMs;
    private LimitStatus limitStatus;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LimitStatus {
        private int usedRequests;
        private int maxRequests;
        private int usedTokens;
        private int maxTokens;
        private String windowType;
        private String windowEndsAt;
    }
}