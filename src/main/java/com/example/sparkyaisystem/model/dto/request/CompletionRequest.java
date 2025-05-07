package com.example.sparkyaisystem.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequest {

    @NotNull(message = "Model ID is required")
    private Long modelId;

    @NotBlank(message = "Prompt is required")
    private String prompt;
    
    private Integer maxTokens;
    
    private Float temperature;
}