package com.example.sparkyaisystem.model.dto.restriction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionRequest {

    @NotNull(message = "Model ID is required")
    private Long modelId;

    @NotNull(message = "Max requests per window is required")
    @Min(value = 1, message = "Max requests per window must be at least 1")
    private Integer maxRequestsPerWindow;

    @NotNull(message = "Max tokens per window is required")
    @Min(value = 1, message = "Max tokens per window must be at least 1")
    private Integer maxTokensPerWindow;

    @NotNull(message = "Window type is required")
    @Pattern(regexp = "^(daily|weekly|monthly)$", message = "Window type must be daily, weekly, or monthly")
    private String windowType;
}