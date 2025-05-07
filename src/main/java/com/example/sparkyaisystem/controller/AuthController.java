package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.exception.EmailAlreadyExistsException;
import com.example.sparkyaisystem.model.dto.auth.LoginRequest;
import com.example.sparkyaisystem.model.dto.auth.LoginResponse;
import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API for login and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with email and password and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Authentication successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(
        summary = "Register a Sparky admin",
        description = "Creates a new Sparky admin user with ROLE_SPARKY_ADMIN privileges"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Admin registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register/admin")
    public ResponseEntity<String> registerSparkyAdmin(
            @Parameter(description = "Admin registration details", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.registerSparkyAdmin(registerRequest);
        return ResponseEntity.ok("Sparky admin registered successfully with ID: " + user.getId());
    }

    @Operation(
        summary = "Register a regular user",
        description = "Creates a new user with ROLE_USER privileges, associated with a company"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register/user")
    public ResponseEntity<String> registerUser(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully with ID: " + user.getId());
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}