package com.example.sparkyaisystem.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long modelId;
    private String modelName;
    private String modelProvider;
    private String modelType;
    private String query;
    private String response;
    private String errorMessage;
    private boolean successful;
    private int tokensConsumed;
    private String fileName;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Long processingTimeMs;
}