package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.exception.EmailAlreadyExistsException;
import com.example.sparkyaisystem.model.dto.auth.LoginRequest;
import com.example.sparkyaisystem.model.dto.auth.LoginResponse;
import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private RegisterRequest registerRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password");

        loginResponse = LoginResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_USER)
                .token("jwt.token.here")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("admin@example.com");
        registerRequest.setPassword("admin123");
        registerRequest.setFirstName("Admin");
        registerRequest.setLastName("User");
        registerRequest.setRole(Role.ROLE_SPARKY_ADMIN);

        mockUser = new User();
        mockUser.setId(100L);
        mockUser.setEmail("admin@example.com");
    }

    @Test
    void shouldLoginSuccessfully() {
        when(authService.login(loginRequest)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("user@example.com", response.getBody().getEmail());
        assertEquals("jwt.token.here", response.getBody().getToken());
    }

    @Test
    void shouldRegisterSparkyAdminSuccessfully() {
        when(authService.registerSparkyAdmin(registerRequest)).thenReturn(mockUser);

        ResponseEntity<String> response = authController.registerSparkyAdmin(registerRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("100"));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(authService.registerUser(registerRequest)).thenReturn(mockUser);

        ResponseEntity<String> response = authController.registerUser(registerRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("100"));
    }

    @Test
    void shouldHandleEmailAlreadyExistsException() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already in use");

        ResponseEntity<String> response = authController.handleEmailAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
    }
}
