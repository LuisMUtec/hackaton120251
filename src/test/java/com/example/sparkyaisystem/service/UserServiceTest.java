package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.dto.user.UserConsumptionResponse;
import com.example.sparkyaisystem.model.dto.user.UserRequest;
import com.example.sparkyaisystem.model.dto.user.UserResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Request;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RequestRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private LimitRepository limitRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    private Company company;
    private UserRequest userRequest;
    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Company fixture
        company = new Company();
        company.setId(100L);
        company.setName("Acme Corp");

        // UserRequest fixture
        userRequest = new UserRequest();
        userRequest.setFirstName("Alice");
        userRequest.setLastName("Smith");
        userRequest.setEmail("alice@example.com");
        userRequest.setPassword("secret");
        userRequest.setRole(null); // assume optional or irrelevant here

        // RegisterRequest that AuthService will receive
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName(userRequest.getFirstName());
        registerRequest.setLastName(userRequest.getLastName());
        registerRequest.setEmail(userRequest.getEmail());
        registerRequest.setPassword(userRequest.getPassword());
        registerRequest.setRole(userRequest.getRole());
        registerRequest.setCompanyId(company.getId());

        // User entity returned by AuthService
        user = new User();
        user.setId(200L);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setCompany(company);
        // initialize collections to avoid NPE
        user.setRequests(new ArrayList<>());
        user.setLimits(new ArrayList<>());
    }

    @Test
    void createUserSuccess() {
        // Arrange
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(authService.registerUser(registerRequest))
                .thenReturn(user);

        // Act
        UserResponse resp = userService.createUser(userRequest, company.getId());

        // Assert
        assertNotNull(resp);
        assertEquals(user.getId(), resp.getId());
        assertEquals("Alice", resp.getFirstName());
        assertEquals("Smith", resp.getLastName());
        assertEquals("alice@example.com", resp.getEmail());
        assertEquals(company.getId(), resp.getCompanyId());
        assertEquals(company.getName(), resp.getCompanyName());
        assertEquals(0, resp.getLimitsCount());
        assertEquals(0, resp.getRequestsCount());

        // Verify interactions
        verify(companyRepository).findById(company.getId());
        verify(authService).registerUser(registerRequest);
    }

    @Test
    void createUserThrowsWhenCompanyNotFound() {
        // Arrange
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.createUser(userRequest, company.getId())
        );
        assertEquals("Company not found", ex.getMessage());

        verify(companyRepository).findById(company.getId());
        verifyNoInteractions(authService);
    }

    @Test
    void getAllUsersByCompanySuccess() {
        // Arrange
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompany(company))
                .thenReturn(List.of(u1, u2));

        // Act
        List<UserResponse> list = userService.getAllUsersByCompany(company.getId());

        // Assert
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());

        verify(companyRepository).findById(company.getId());
        verify(userRepository).findByCompany(company);
    }

    @Test
    void getAllUsersByCompanyThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.getAllUsersByCompany(company.getId())
        );
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void getUserByIdSuccess() {
        // Arrange
        long userId = 200L;
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, userId))
                .thenReturn(Optional.of(user));

        // Act
        UserResponse resp = userService.getUserById(company.getId(), userId);

        // Assert
        assertEquals(user.getId(), resp.getId());
        assertEquals("alice@example.com", resp.getEmail());

        verify(companyRepository).findById(company.getId());
        verify(userRepository).findByCompanyAndId(company, userId);
    }

    @Test
    void getUserByIdThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.getUserById(company.getId(), 999L)
        );
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void getUserByIdThrowsWhenUserNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, 999L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.getUserById(company.getId(), 999L)
        );
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void updateUserSuccessEmailUnchanged() {
        // Arrange
        long userId = user.getId();
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, userId))
                .thenReturn(Optional.of(user));
        // same email → no existsByEmail check
        when(authService.updateUser(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponse resp = userService.updateUser(company.getId(), userId, userRequest);

        // Assert
        assertEquals(userId, resp.getId());
        verify(authService).updateUser(userRequest);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserSuccessEmailChanged() {
        // Arrange
        long userId = user.getId();
        UserRequest req = new UserRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("new@example.com");
        req.setPassword("secret");
        req.setRole(null);

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(req.getEmail()))
                .thenReturn(false);
        when(authService.updateUser(req)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponse resp = userService.updateUser(company.getId(), userId, req);

        // Assert
        assertEquals(userId, resp.getId());
        verify(userRepository).existsByEmail(req.getEmail());
        verify(authService).updateUser(req);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserThrowsWhenEmailExists() {
        // Arrange
        long userId = user.getId();
        UserRequest req = new UserRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("taken@example.com");
        req.setPassword("secret");
        req.setRole(null);

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(req.getEmail()))
                .thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.updateUser(company.getId(), userId, req)
        );
        assertEquals("Email is already in use", ex.getMessage());
        verify(userRepository).existsByEmail(req.getEmail());
        verify(authService, never()).updateUser(any());
    }

    @Test
    void updateUserThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.updateUser(company.getId(), 123L, userRequest)
        );
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void updateUserThrowsWhenUserNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, 999L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.updateUser(company.getId(), 999L, userRequest)
        );
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUserConsumptionSuccess() {
        // Arrange
        long userId = user.getId();
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, userId))
                .thenReturn(Optional.of(user));

        // Prepare requests
        AIModel m1 = new AIModel(); m1.setId(1L); m1.setName("M1");
        AIModel m2 = new AIModel(); m2.setId(2L); m2.setName("M2");

        Request r1 = new Request();
        r1.setModel(m1);
        r1.setTokensConsumed(10);
        Request r2 = new Request();
        r2.setModel(m2);
        r2.setTokensConsumed(20);

        when(requestRepository.findByUser(user))
                .thenReturn(List.of(r1, r2));

        // Prepare limits
        Limit lim1 = new Limit();
        lim1.setModel(m1);
        lim1.setMaxRequestsPerWindow(5);
        lim1.setMaxTokensPerWindow(100);

        when(limitRepository.findByUser(user))
                .thenReturn(List.of(lim1));

        // Act
        UserConsumptionResponse resp = userService.getUserConsumption(company.getId(), userId);

        // Assert totals
        assertEquals(userId, resp.getUserId());
        assertEquals(2, resp.getTotalRequests());
        assertEquals(30, resp.getTotalTokensConsumed());

        // Assert per-model consumption
        Map<Long, UserConsumptionResponse.ModelConsumption> map = new HashMap<>();
        resp.getModelConsumptions().forEach(mc -> map.put(mc.getModelId(), mc));

        var mc1 = map.get(1L);
        assertNotNull(mc1);
        assertEquals(1, mc1.getRequestsCount());
        assertEquals(10, mc1.getTokensConsumed());
        assertEquals(5, mc1.getMaxRequestsAllowed());
        assertEquals(100, mc1.getMaxTokensAllowed());

        var mc2 = map.get(2L);
        assertNotNull(mc2);
        assertEquals(1, mc2.getRequestsCount());
        assertEquals(20, mc2.getTokensConsumed());
        assertEquals(0, mc2.getMaxRequestsAllowed());
        assertEquals(0, mc2.getMaxTokensAllowed());

        verify(requestRepository).findByUser(user);
        verify(limitRepository).findByUser(user);
    }

    @Test
    void getUserConsumptionThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.getUserConsumption(company.getId(), 123L)
        );
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void getUserConsumptionThrowsWhenUserNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(company, 999L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.getUserConsumption(company.getId(), 999L)
        );
        assertEquals("User not found", ex.getMessage());
    }
}
