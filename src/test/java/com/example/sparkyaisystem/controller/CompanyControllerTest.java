package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.limit.LimitRequest;
import com.example.sparkyaisystem.model.dto.limit.LimitResponse;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionRequest;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionResponse;
import com.example.sparkyaisystem.model.dto.user.UserConsumptionResponse;
import com.example.sparkyaisystem.model.dto.user.UserRequest;
import com.example.sparkyaisystem.model.dto.user.UserResponse;
import com.example.sparkyaisystem.security.JwtTokenProvider;
import com.example.sparkyaisystem.service.LimitService;
import com.example.sparkyaisystem.service.RestrictionService;
import com.example.sparkyaisystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyControllerTest {

    @Mock private RestrictionService restrictionService;
    @Mock private UserService userService;
    @Mock private LimitService limitService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest request;

    @InjectMocks private CompanyController controller;

    private final Long companyId = 10L;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(jwtTokenProvider.resolveToken(request)).thenReturn("token");
        when(jwtTokenProvider.getCompanyId("token")).thenReturn(companyId);
    }

    // --- Restricciones ---
    @Test
    void shouldCreateRestriction() {
        RestrictionRequest req = new RestrictionRequest();
        RestrictionResponse res = new RestrictionResponse();
        when(restrictionService.createRestriction(eq(companyId), any())).thenReturn(res);

        var response = controller.createRestriction(request, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldGetAllRestrictions() {
        when(restrictionService.getRestrictionsByCompany(companyId)).thenReturn(List.of(new RestrictionResponse()));

        var response = controller.getAllRestrictions(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldUpdateRestriction() {
        RestrictionRequest req = new RestrictionRequest();
        RestrictionResponse res = new RestrictionResponse();
        when(restrictionService.updateRestriction(companyId, 5L, req)).thenReturn(res);

        var response = controller.updateRestriction(request, 5L, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteRestriction() {
        var response = controller.deleteRestriction(request, 3L);
        verify(restrictionService).deleteRestriction(companyId, 3L);
        assertEquals(204, response.getStatusCodeValue());
    }

    // --- Usuarios ---
    @Test
    void shouldCreateUser() {
        UserRequest req = new UserRequest();
        UserResponse res = new UserResponse();
        when(userService.createUser(req, companyId)).thenReturn(res);

        var response = controller.createUser(request, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldGetAllUsers() {
        when(userService.getAllUsersByCompany(companyId)).thenReturn(List.of(new UserResponse()));

        var response = controller.getAllUsers(request);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldGetUserById() {
        UserResponse res = new UserResponse();
        when(userService.getUserById(companyId, 7L)).thenReturn(res);

        var response = controller.getUserById(request, 7L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateUser() {
        UserRequest req = new UserRequest();
        UserResponse res = new UserResponse();
        when(userService.updateUser(companyId, 4L, req)).thenReturn(res);

        var response = controller.updateUser(request, 4L, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldGetUserConsumption() {
        UserConsumptionResponse res = new UserConsumptionResponse();
        when(userService.getUserConsumption(companyId, 8L)).thenReturn(res);

        var response = controller.getUserConsumption(request, 8L);
        assertEquals(200, response.getStatusCodeValue());
    }

    // --- Límites ---
    @Test
    void shouldCreateLimit() {
        LimitRequest req = new LimitRequest();
        req.setUserId(5L);

        LimitResponse res = new LimitResponse();
        when(limitService.createLimit(companyId, req)).thenReturn(res);

        var response = controller.createLimit(request, 5L, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldGetUserLimits() {
        when(limitService.getLimitsByUser(companyId, 6L)).thenReturn(List.of(new LimitResponse()));

        var response = controller.getUserLimits(request, 6L);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldUpdateLimit() {
        LimitRequest req = new LimitRequest();
        req.setUserId(7L);
        LimitResponse res = new LimitResponse();
        when(limitService.updateLimit(companyId, 77L, req)).thenReturn(res);

        var response = controller.updateLimit(request, 7L, 77L, req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteLimit() {
        var response = controller.deleteLimit(request, 9L, 88L);
        verify(limitService).deleteLimit(companyId, 88L);
        assertEquals(204, response.getStatusCodeValue());
    }
}
