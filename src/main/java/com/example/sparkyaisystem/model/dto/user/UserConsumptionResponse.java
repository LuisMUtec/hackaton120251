package com.example.sparkyaisystem.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConsumptionResponse {
    private Long userId;
    private String userName;
    private String email;
    private Long companyId;
    private String companyName;
    private int totalRequests;
    private int totalTokensConsumed;
    private List<ModelConsumption> modelConsumptions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelConsumption {
        private Long modelId;
        private String modelName;
        private String modelProvider;
        private int requestsCount;
        private int tokensConsumed;
        private int maxRequestsAllowed;
        private int maxTokensAllowed;
    }
}