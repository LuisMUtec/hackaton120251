package com.example.sparkyaisystem.model.dto.company;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatusRequest {
    
    @NotNull(message = "Active status is required")
    private Boolean active;
}