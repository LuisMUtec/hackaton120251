package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.exception.EmailAlreadyExistsException;
import com.example.sparkyaisystem.model.dto.company.CompanyConsumptionResponse;
import com.example.sparkyaisystem.model.dto.company.CompanyRequest;
import com.example.sparkyaisystem.model.dto.company.CompanyResponse;
import com.example.sparkyaisystem.model.dto.company.CompanyStatusRequest;
import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Request;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.RequestRepository;
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
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest companyRequest;
    private CompanyStatusRequest statusRequest;
    private Company sampleCompany;
    private User adminUser;

    @BeforeEach
    void setUp() {
        companyRequest = new CompanyRequest();
        companyRequest.setName("TestCo");
        companyRequest.setRuc("123456789");
        companyRequest.setActive(true);
        companyRequest.setAdminFirstName("Admin");
        companyRequest.setAdminLastName("User");
        companyRequest.setAdminEmail("admin@testco.com");
        companyRequest.setAdminPassword("pass123");

        statusRequest = new CompanyStatusRequest();
        statusRequest.setActive(false);

        sampleCompany = new Company();
        sampleCompany.setId(1L);
        sampleCompany.setName("TestCo");
        sampleCompany.setRuc("123456789");
        sampleCompany.setActive(true);
        sampleCompany.setAffiliationDate(LocalDateTime.now());

        adminUser = new User();
        adminUser.setId(10L);
        adminUser.setEmail(companyRequest.getAdminEmail());
    }

    @Test
    void createCompanySuccess() {
        // Arrange
        when(companyRepository.existsByName(eq(companyRequest.getName()))).thenReturn(false);
        when(companyRepository.existsByRuc(eq(companyRequest.getRuc()))).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(sampleCompany.getId());
            return c;
        });
        when(authService.registerCompanyAdmin(any(RegisterRequest.class), any(Company.class)))
                .thenReturn(adminUser);
        // Act
        CompanyResponse response = companyService.createCompany(companyRequest);

        // Assert
        assertNotNull(response);
        assertEquals(sampleCompany.getId(), response.getId());
        assertEquals(companyRequest.getName(), response.getName());
        assertEquals(companyRequest.getRuc(), response.getRuc());
        assertEquals(adminUser.getId(), response.getAdminId());
        assertTrue(response.isActive());
        verify(companyRepository, times(2)).save(any(Company.class));
        verify(authService).registerCompanyAdmin(any(RegisterRequest.class), any(Company.class));
    }

    @Test
    void createCompanyThrowsWhenNameExists() {
        // Arrange
        when(companyRepository.existsByName(eq(companyRequest.getName()))).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.createCompany(companyRequest));
        assertTrue(ex.getMessage().contains("name already exists"));
        verify(companyRepository).existsByName(companyRequest.getName());
        verifyNoMoreInteractions(companyRepository);
    }

    @Test
    void createCompanyThrowsWhenRucExists() {
        // Arrange
        when(companyRepository.existsByName(eq(companyRequest.getName()))).thenReturn(false);
        when(companyRepository.existsByRuc(eq(companyRequest.getRuc()))).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.createCompany(companyRequest));
        assertTrue(ex.getMessage().contains("RUC already exists"));
        verify(companyRepository).existsByName(companyRequest.getName());
        verify(companyRepository).existsByRuc(companyRequest.getRuc());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void getAllCompaniesReturnsList() {
        // Arrange
        when(companyRepository.findAll()).thenReturn(List.of(sampleCompany));
        // Act
        List<CompanyResponse> list = companyService.getAllCompanies();
        // Assert
        assertEquals(1, list.size());
        assertEquals(sampleCompany.getName(), list.get(0).getName());
        verify(companyRepository).findAll();
    }

    @Test
    void getCompanyByIdThrowsWhenNotFound() {
        // Arrange
        when(companyRepository.findById(eq(2L))).thenReturn(Optional.empty());
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.getCompanyById(2L));
        assertTrue(ex.getMessage().contains("Company not found"));
        verify(companyRepository).findById(2L);
    }

    @Test
    void updateCompanySuccess() {
        // Arrange
        CompanyRequest req = new CompanyRequest();
        req.setName("NewCo");
        req.setRuc("987654321");
        req.setActive(false);
        when(companyRepository.findById(eq(sampleCompany.getId()))).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.existsByName(eq(req.getName()))).thenReturn(false);
        when(companyRepository.existsByRuc(eq(req.getRuc()))).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));
        // Act
        CompanyResponse resp = companyService.updateCompany(sampleCompany.getId(), req);
        // Assert
        assertEquals(req.getName(), resp.getName());
        assertFalse(resp.isActive());
        verify(companyRepository).findById(sampleCompany.getId());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void updateCompanyStatusUpdatesActiveFlag() {
        // Arrange
        when(companyRepository.findById(eq(sampleCompany.getId()))).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);
        // Act
        CompanyResponse resp = companyService.updateCompanyStatus(sampleCompany.getId(), statusRequest);
        // Assert
        assertFalse(resp.isActive());
        verify(companyRepository).findById(sampleCompany.getId());
        verify(companyRepository).save(sampleCompany);
    }

    @Test
    void getCompanyConsumptionAggregatesData() {
        // Arrange
        Request r1 = new Request(); r1.setTokensConsumed(10);
        AIModel m1 = new AIModel(); m1.setId(100L); m1.setName("M1"); m1.setProvider("P1"); r1.setModel(m1);
        Request r2 = new Request(); r2.setTokensConsumed(20); r2.setModel(m1);
        when(companyRepository.findById(eq(sampleCompany.getId()))).thenReturn(Optional.of(sampleCompany));
        when(requestRepository.findByCompany(eq(sampleCompany))).thenReturn(List.of(r1, r2));
        // Act
        CompanyConsumptionResponse report = companyService.getCompanyConsumption(sampleCompany.getId());
        // Assert
        assertEquals(sampleCompany.getId(), report.getCompanyId());
        assertEquals(2, report.getTotalRequests());
        assertEquals(30, report.getTotalTokensConsumed());
        assertEquals(1, report.getModelConsumptions().size());
        assertEquals(m1.getId(), report.getModelConsumptions().get(0).getModelId());
        verify(companyRepository).findById(sampleCompany.getId());
        verify(requestRepository).findByCompany(sampleCompany);
    }
}
