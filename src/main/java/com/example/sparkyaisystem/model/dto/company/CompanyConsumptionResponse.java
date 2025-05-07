package com.example.sparkyaisystem.model.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyConsumptionResponse {
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
    }
}