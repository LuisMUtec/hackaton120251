package com.example.sparkyaisystem.model.dto.limit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long modelId;
    private String modelName;
    private String modelProvider;
    private String modelType;
    private int maxRequestsPerWindow;
    private int maxTokensPerWindow;
    private int usedRequests;
    private int usedTokens;
    private String windowType;
    private LocalDateTime windowStartTime;
    private LocalDateTime windowEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}