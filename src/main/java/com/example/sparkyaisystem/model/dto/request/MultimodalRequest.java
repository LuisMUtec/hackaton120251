package com.example.sparkyaisystem.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultimodalRequest {

    @NotNull(message = "Model ID is required")
    private Long modelId;

    @NotBlank(message = "Message content is required")
    private String message;
    
    @NotNull(message = "Image file is required")
    private MultipartFile imageFile;
}