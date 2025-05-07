package com.example.sparkyaisystem.model.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String ruc;
    private LocalDateTime affiliationDate;
    private boolean active;
    private Long adminId;
    private String adminName;
    private String adminEmail;
    private int totalUsers;
    private int totalRestrictions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}