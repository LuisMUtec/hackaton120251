package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.limit.LimitRequest;
import com.example.sparkyaisystem.model.dto.limit.LimitResponse;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionRequest;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionResponse;
import com.example.sparkyaisystem.model.dto.user.UserConsumptionResponse;
import com.example.sparkyaisystem.model.dto.user.UserRequest;
import com.example.sparkyaisystem.model.dto.user.UserResponse;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.security.JwtTokenProvider;
import com.example.sparkyaisystem.service.LimitService;
import com.example.sparkyaisystem.service.RestrictionService;
import com.example.sparkyaisystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class CompanyController {

    private final RestrictionService restrictionService;
    private final UserService userService;
    private final LimitService limitService;
    private final JwtTokenProvider jwtTokenProvider;

    public CompanyController(RestrictionService restrictionService,
                             UserService userService,
                             LimitService limitService,
                             JwtTokenProvider jwtTokenProvider) {
        this.restrictionService = restrictionService;
        this.userService = userService;
        this.limitService = limitService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Helper method to get company ID from JWT token
    private Long getCompanyIdFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        return jwtTokenProvider.getCompanyId(token);
    }

    // Restriction management endpoints
    @PostMapping("/restrictions")
    public ResponseEntity<RestrictionResponse> createRestriction(
            HttpServletRequest request,
            @Valid @RequestBody RestrictionRequest restrictionRequest) {
        Long companyId = getCompanyIdFromToken(request);
        RestrictionResponse response = restrictionService.createRestriction(companyId, restrictionRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restrictions")
    public ResponseEntity<List<RestrictionResponse>> getAllRestrictions(HttpServletRequest request) {
        Long companyId = getCompanyIdFromToken(request);
        List<RestrictionResponse> restrictions = restrictionService.getRestrictionsByCompany(companyId);
        return ResponseEntity.ok(restrictions);
    }

    @PutMapping("/restrictions/{id}")
    public ResponseEntity<RestrictionResponse> updateRestriction(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody RestrictionRequest restrictionRequest) {
        Long companyId = getCompanyIdFromToken(request);
        RestrictionResponse response = restrictionService.updateRestriction(companyId, id, restrictionRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/restrictions/{id}")
    public ResponseEntity<Void> deleteRestriction(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long companyId = getCompanyIdFromToken(request);
        restrictionService.deleteRestriction(companyId, id);
        return ResponseEntity.noContent().build();
    }

    // User management endpoints
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
            HttpServletRequest request,
            @Valid @RequestBody UserRequest userRequest) {
        Long companyId = getCompanyIdFromToken(request);
        UserResponse response = userService.createUser(userRequest, companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        Long companyId = getCompanyIdFromToken(request);
        List<UserResponse> users = userService.getAllUsersByCompany(companyId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long companyId = getCompanyIdFromToken(request);
        UserResponse response = userService.getUserById(companyId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        Long companyId = getCompanyIdFromToken(request);
        UserResponse response = userService.updateUser(companyId, id, userRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/consumption")
    public ResponseEntity<UserConsumptionResponse> getUserConsumption(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long companyId = getCompanyIdFromToken(request);
        UserConsumptionResponse response = userService.getUserConsumption(companyId, id);
        return ResponseEntity.ok(response);
    }

    // Limit management endpoints
    @PostMapping("/users/{id}/limits")
    public ResponseEntity<LimitResponse> createLimit(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody LimitRequest limitRequest) {
        Long companyId = getCompanyIdFromToken(request);
        // Ensure the limit is for the specified user
        limitRequest.setUserId(id);
        LimitResponse response = limitService.createLimit(companyId, limitRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/limits")
    public ResponseEntity<List<LimitResponse>> getUserLimits(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long companyId = getCompanyIdFromToken(request);
        List<LimitResponse> limits = limitService.getLimitsByUser(companyId, id);
        return ResponseEntity.ok(limits);
    }

    @PutMapping("/users/{userId}/limits/{limitId}")
    public ResponseEntity<LimitResponse> updateLimit(
            HttpServletRequest request,
            @PathVariable Long userId,
            @PathVariable Long limitId,
            @Valid @RequestBody LimitRequest limitRequest) {
        Long companyId = getCompanyIdFromToken(request);
        // Ensure the limit is for the specified user
        limitRequest.setUserId(userId);
        LimitResponse response = limitService.updateLimit(companyId, limitId, limitRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/limits/{limitId}")
    public ResponseEntity<Void> deleteLimit(
            HttpServletRequest request,
            @PathVariable Long userId,
            @PathVariable Long limitId) {
        Long companyId = getCompanyIdFromToken(request);
        limitService.deleteLimit(companyId, limitId);
        return ResponseEntity.noContent().build();
    }
}