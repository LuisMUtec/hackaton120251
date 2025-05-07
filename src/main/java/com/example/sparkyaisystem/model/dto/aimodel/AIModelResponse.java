package com.example.sparkyaisystem.model.dto.aimodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIModelResponse {
    private Long id;
    private String name;
    private String provider;
    private String type;
    private boolean active;
    private String description;
    private boolean available;
    private Integer maxTokensPerRequest;
}