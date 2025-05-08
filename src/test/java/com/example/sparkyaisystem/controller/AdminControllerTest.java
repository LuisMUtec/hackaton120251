package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.company.*;
import com.example.sparkyaisystem.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa @Mock y @InjectMocks
    }

    @Test
    void shouldCreateCompany() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .ruc("12345678901")
                .adminFirstName("Alice")
                .adminLastName("Smith")
                .adminEmail("alice@example.com")
                .adminPassword("password")
                .active(true)
                .build();

        CompanyResponse expectedResponse = CompanyResponse.builder()
                .id(1L)
                .name("Acme Corp")
                .ruc("12345678901")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(companyService.createCompany(request)).thenReturn(expectedResponse);

        ResponseEntity<CompanyResponse> response = adminController.createCompany(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Acme Corp", response.getBody().getName());
    }

    @Test
    void shouldGetAllCompanies() {
        when(companyService.getAllCompanies()).thenReturn(List.of(
                CompanyResponse.builder().id(1L).name("C1").build(),
                CompanyResponse.builder().id(2L).name("C2").build()
        ));

        ResponseEntity<List<CompanyResponse>> response = adminController.getAllCompanies();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void shouldGetCompanyById() {
        CompanyResponse expected = CompanyResponse.builder().id(1L).name("Test").build();
        when(companyService.getCompanyById(1L)).thenReturn(expected);

        var response = adminController.getCompanyById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Test", response.getBody().getName());
    }

    @Test
    void shouldUpdateCompany() {
        CompanyRequest update = CompanyRequest.builder()
                .name("Updated")
                .ruc("11111111111")
                .adminFirstName("U")
                .adminLastName("P")
                .adminEmail("up@test.com")
                .adminPassword("123")
                .active(false)
                .build();

        CompanyResponse updated = CompanyResponse.builder()
                .id(1L).name("Updated").ruc("11111111111").active(false).build();

        when(companyService.updateCompany(eq(1L), any())).thenReturn(updated);

        var response = adminController.updateCompany(1L, update);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated", response.getBody().getName());
    }

    @Test
    void shouldUpdateCompanyStatus() {
        CompanyStatusRequest statusRequest = new CompanyStatusRequest();
        statusRequest.setActive(false);

        CompanyResponse expected = CompanyResponse.builder().id(1L).active(false).build();
        when(companyService.updateCompanyStatus(1L, statusRequest)).thenReturn(expected);

        var response = adminController.updateCompanyStatus(1L, statusRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isActive());
    }

    @Test
    void shouldGetCompanyConsumption() {
        CompanyConsumptionResponse consumption = CompanyConsumptionResponse.builder()
                .companyId(1L)
                .companyName("Acme")
                .totalRequests(50)
                .totalTokensConsumed(1000)
                .modelConsumptions(List.of())
                .build();

        when(companyService.getCompanyConsumption(1L)).thenReturn(consumption);

        var response = adminController.getCompanyConsumption(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Acme", response.getBody().getCompanyName());
    }
}
