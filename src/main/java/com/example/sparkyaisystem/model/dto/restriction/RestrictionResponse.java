package com.example.sparkyaisystem.model.dto.restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long modelId;
    private String modelName;
    private String modelProvider;
    private String modelType;
    private int maxRequestsPerWindow;
    private int maxTokensPerWindow;
    private String windowType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}