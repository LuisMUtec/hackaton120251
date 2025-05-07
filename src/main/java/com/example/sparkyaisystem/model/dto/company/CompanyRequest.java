package com.example.sparkyaisystem.model.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    private String name;

    @NotBlank(message = "RUC is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "RUC must be 11 digits")
    private String ruc;

    private boolean active = true;

    // Admin information
    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;

    @NotBlank(message = "Admin email is required")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    private String adminPassword;
}