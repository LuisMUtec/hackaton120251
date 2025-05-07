package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.auth.LoginRequest;
import com.example.sparkyaisystem.model.dto.auth.LoginResponse;
import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import com.example.sparkyaisystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_USER);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("password");
        registerRequest.setRole(Role.ROLE_USER);
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.createToken(eq(testUser.getEmail()), anyList(), eq(testUser.getId()), isNull()))
                .thenReturn("jwt.token.string");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.string", response.getToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getRole(), response.getRole());

        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtTokenProvider).createToken(eq(testUser.getEmail()), anyList(), eq(testUser.getId()), isNull());
    }

    @Test
    void registerSparkyAdminShouldCreateNewAdminUser() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(registerRequest.getEmail());
        savedUser.setFirstName(registerRequest.getFirstName());
        savedUser.setLastName(registerRequest.getLastName());
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.ROLE_SPARKY_ADMIN);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.registerSparkyAdmin(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(registerRequest.getEmail(), result.getEmail());
        assertEquals(registerRequest.getFirstName(), result.getFirstName());
        assertEquals(registerRequest.getLastName(), result.getLastName());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.ROLE_SPARKY_ADMIN, result.getRole());

        // Verify
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.registerSparkyAdmin(registerRequest);
        });

        assertEquals("Email is already in use", exception.getMessage());

        // Verify
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
}