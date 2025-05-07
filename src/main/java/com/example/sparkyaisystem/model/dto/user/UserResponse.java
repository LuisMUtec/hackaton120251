package com.example.sparkyaisystem.model.dto.user;

import com.example.sparkyaisystem.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Long companyId;
    private String companyName;
    private int limitsCount;
    private int requestsCount;
}