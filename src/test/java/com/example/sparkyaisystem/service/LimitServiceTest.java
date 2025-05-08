package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.exception.LimitExceededException;
import com.example.sparkyaisystem.model.dto.limit.LimitRequest;
import com.example.sparkyaisystem.model.dto.limit.LimitResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitServiceTest {

    @Mock
    private LimitRepository limitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AIModelRepository aiModelRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RestrictionRepository restrictionRepository;

    @InjectMocks
    private LimitService limitService;

    private Company company;
    private User user;
    private AIModel model;
    private Restriction restriction;
    private LimitRequest request;
    private Limit limit;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);

        user = new User();
        user.setId(2L);
        user.setCompany(company);

        model = new AIModel();
        model.setId(3L);

        restriction = new Restriction();
        restriction.setCompany(company);
        restriction.setModel(model);
        restriction.setMaxRequestsPerWindow(5);
        restriction.setMaxTokensPerWindow(100);

        request = new LimitRequest();
        request.setUserId(user.getId());
        request.setModelId(model.getId());
        request.setMaxRequestsPerWindow(4);
        request.setMaxTokensPerWindow(50);
        request.setWindowType("daily");

        limit = new Limit();
        limit.setId(10L);
        limit.setUser(user);
        limit.setModel(model);
        limit.setMaxRequestsPerWindow(request.getMaxRequestsPerWindow());
        limit.setMaxTokensPerWindow(request.getMaxTokensPerWindow());
        limit.setWindowType(request.getWindowType());
        limit.setUsedRequests(0);
        limit.setUsedTokens(0);
        limit.setWindowStartTime(LocalDateTime.now().minusHours(1));
        limit.setWindowEndTime(LocalDateTime.now().plusHours(23));
    }

    @Test
    void createLimitSuccess() {
        // Arrange
        when(companyRepository.findById(eq(company.getId()))).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(eq(company), eq(user.getId()))).thenReturn(Optional.of(user));
        when(aiModelRepository.findById(eq(model.getId()))).thenReturn(Optional.of(model));
        when(restrictionRepository.findByCompanyAndModel(eq(company), eq(model)))
                .thenReturn(Optional.of(restriction));
        when(limitRepository.existsByUserAndModel(eq(user), eq(model))).thenReturn(false);
        when(limitRepository.save(any(Limit.class))).thenReturn(limit);

        // Act
        LimitResponse response = limitService.createLimit(company.getId(), request);

        // Assert
        assertNotNull(response);
        assertEquals(limit.getId(), response.getId());
        assertEquals(request.getMaxRequestsPerWindow(), response.getMaxRequestsPerWindow());
        assertEquals(request.getMaxTokensPerWindow(), response.getMaxTokensPerWindow());
        verify(limitRepository).save(any(Limit.class));
    }

    @Test
    void createLimitThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> limitService.createLimit(99L, request));
        assertTrue(ex.getMessage().contains("Company not found"));
    }

    @Test
    void getLimitsByUserSuccess() {
        when(companyRepository.findById(eq(company.getId()))).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyAndId(eq(company), eq(user.getId()))).thenReturn(Optional.of(user));

        when(limitRepository.findByUser(eq(user))).thenReturn(List.of(limit));

        List<LimitResponse> list = limitService.getLimitsByUser(company.getId(), user.getId());
        assertEquals(1, list.size());
        assertEquals(limit.getId(), list.get(0).getId());
    }

    @Test
    void getLimitByIdSuccess() {
        when(companyRepository.findById(eq(company.getId()))).thenReturn(Optional.of(company));
        when(limitRepository.findById(eq(limit.getId()))).thenReturn(Optional.of(limit));

        LimitResponse resp = limitService.getLimitById(company.getId(), limit.getId());
        assertEquals(limit.getId(), resp.getId());
    }

    @Test
    void getLimitByIdThrowsWhenMismatchCompany() {
        Company other = new Company(); other.setId(5L);
        user.setCompany(other);
        when(companyRepository.findById(eq(company.getId()))).thenReturn(Optional.of(company));
        when(limitRepository.findById(eq(limit.getId()))).thenReturn(Optional.of(limit));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> limitService.getLimitById(company.getId(), limit.getId()));
        assertTrue(ex.getMessage().contains("does not belong"));
    }

    @Test
    void checkAndUpdateLimitSuccess() {
        limit.setUsedRequests(1);
        limit.setUsedTokens(10);
        when(limitRepository.findByUserAndModel(eq(user), eq(model)))
                .thenReturn(Optional.of(limit));

        // Act: consume 5 tokens
        limitService.checkAndUpdateLimit(user, model, 5);

        // Assert
        assertEquals(2, limit.getUsedRequests());
        assertEquals(15, limit.getUsedTokens());
        verify(limitRepository).save(limit);
    }

    @Test
    void checkAndUpdateLimitThrowsWhenRequestsExceeded() {
        limit.setUsedRequests(5); // at max
        limit.setWindowEndTime(LocalDateTime.now().plusHours(1));
        when(limitRepository.findByUserAndModel(eq(user), eq(model)))
                .thenReturn(Optional.of(limit));

        LimitExceededException ex = assertThrows(LimitExceededException.class,
                () -> limitService.checkAndUpdateLimit(user, model, 1));
        assertEquals("requests", ex.getLimitType());
    }

    @Test
    void checkAndUpdateLimitThrowsWhenTokensExceeded() {
        limit.setUsedRequests(1);
        limit.setUsedTokens(100);
        when(limitRepository.findByUserAndModel(eq(user), eq(model)))
                .thenReturn(Optional.of(limit));

        LimitExceededException ex = assertThrows(LimitExceededException.class,
                () -> limitService.checkAndUpdateLimit(user, model, 1));
        assertEquals("tokens", ex.getLimitType());
    }
}
