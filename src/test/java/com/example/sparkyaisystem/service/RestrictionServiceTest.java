package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.restriction.RestrictionRequest;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestrictionServiceTest {

    @Mock
    private RestrictionRepository restrictionRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AIModelRepository aiModelRepository;

    @InjectMocks
    private RestrictionService restrictionService;

    private Company company;
    private AIModel model;
    private Restriction restriction;
    private RestrictionRequest request;

    @BeforeEach
    void setUp() {
        // Entidad Company
        company = new Company();
        company.setId(1L);
        company.setName("TestCo");

        // Entidad AIModel
        model = new AIModel();
        model.setId(2L);
        model.setName("TestModel");
        model.setProvider("OpenAI");
        model.setType("chat");

        // Entidad Restriction
        restriction = new Restriction();
        restriction.setId(3L);
        restriction.setCompany(company);
        restriction.setModel(model);
        restriction.setMaxRequestsPerWindow(100);
        restriction.setMaxTokensPerWindow(1000);
        restriction.setWindowType("daily");

        // DTO de entrada
        request = new RestrictionRequest();
        request.setModelId(model.getId());
        request.setMaxRequestsPerWindow(50);
        request.setMaxTokensPerWindow(500);
        request.setWindowType("daily");
    }

    @Test
    void createRestrictionSuccess() {
        // Arrange
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(aiModelRepository.findById(model.getId()))
                .thenReturn(Optional.of(model));
        when(restrictionRepository.existsByCompanyAndModel(company, model))
                .thenReturn(false);
        when(restrictionRepository.save(any(Restriction.class)))
                .thenAnswer(inv -> {
                    Restriction r = inv.getArgument(0);
                    r.setId(restriction.getId());
                    return r;
                });

        // Act
        RestrictionResponse resp = restrictionService.createRestriction(company.getId(), request);

        // Assert
        assertNotNull(resp);
        assertEquals(restriction.getId(), resp.getId());
        assertEquals(company.getId(), resp.getCompanyId());
        assertEquals(model.getId(), resp.getModelId());
        assertEquals(request.getMaxRequestsPerWindow(), resp.getMaxRequestsPerWindow());
        assertEquals(request.getMaxTokensPerWindow(), resp.getMaxTokensPerWindow());
        assertEquals(request.getWindowType(), resp.getWindowType());

        // Verify interactions
        verify(companyRepository).findById(company.getId());
        verify(aiModelRepository).findById(model.getId());
        verify(restrictionRepository).existsByCompanyAndModel(company, model);
        verify(restrictionRepository).save(any(Restriction.class));
    }

    @Test
    void createRestrictionThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.createRestriction(company.getId(), request)
        );
        assertEquals("Company not found", ex.getMessage());

        verify(companyRepository).findById(company.getId());
        verifyNoInteractions(aiModelRepository, restrictionRepository);
    }

    @Test
    void createRestrictionThrowsWhenModelNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(aiModelRepository.findById(model.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.createRestriction(company.getId(), request)
        );
        assertEquals("AI Model not found", ex.getMessage());

        verify(companyRepository).findById(company.getId());
        verify(aiModelRepository).findById(model.getId());
        verifyNoInteractions(restrictionRepository);
    }

    @Test
    void createRestrictionThrowsWhenAlreadyExists() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(aiModelRepository.findById(model.getId()))
                .thenReturn(Optional.of(model));
        when(restrictionRepository.existsByCompanyAndModel(company, model))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.createRestriction(company.getId(), request)
        );
        assertEquals("Restriction already exists for this company and model", ex.getMessage());

        verify(restrictionRepository).existsByCompanyAndModel(company, model);
        verify(restrictionRepository, never()).save(any());
    }

    @Test
    void getRestrictionsByCompanySuccess() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findByCompany(company))
                .thenReturn(List.of(restriction));

        List<RestrictionResponse> list = restrictionService.getRestrictionsByCompany(company.getId());

        assertEquals(1, list.size());
        RestrictionResponse resp = list.get(0);
        assertEquals(restriction.getId(), resp.getId());
        assertEquals(company.getId(), resp.getCompanyId());
        assertEquals(model.getId(), resp.getModelId());

        verify(companyRepository).findById(company.getId());
        verify(restrictionRepository).findByCompany(company);
    }

    @Test
    void getRestrictionsByCompanyThrowsWhenCompanyNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.getRestrictionsByCompany(company.getId())
        );
        assertEquals("Company not found", ex.getMessage());
    }

    @Test
    void getRestrictionByIdSuccess() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));

        RestrictionResponse resp = restrictionService.getRestrictionById(company.getId(), restriction.getId());

        assertNotNull(resp);
        assertEquals(restriction.getId(), resp.getId());

        verify(companyRepository).findById(company.getId());
        verify(restrictionRepository).findById(restriction.getId());
    }

    @Test
    void getRestrictionByIdThrowsWhenNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.getRestrictionById(company.getId(), restriction.getId())
        );
        assertEquals("Restriction not found", ex.getMessage());
    }

    @Test
    void getRestrictionByIdThrowsWhenBelongsToAnotherCompany() {
        Company other = new Company();
        other.setId(99L);
        restriction.setCompany(other);

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.getRestrictionById(company.getId(), restriction.getId())
        );
        assertEquals("Restriction does not belong to the company", ex.getMessage());
    }

    @Test
    void updateRestrictionSuccess() {
        // Arrange nuevos valores
        Long newModelId = 5L;
        AIModel newModel = new AIModel();
        newModel.setId(newModelId);
        newModel.setName("NewModel");
        newModel.setProvider("Meta");
        newModel.setType("completion");

        RestrictionRequest updateReq = new RestrictionRequest();
        updateReq.setModelId(newModelId);
        updateReq.setMaxRequestsPerWindow(200);
        updateReq.setMaxTokensPerWindow(2000);
        updateReq.setWindowType("weekly");

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));
        when(aiModelRepository.findById(newModelId))
                .thenReturn(Optional.of(newModel));
        when(restrictionRepository.existsByCompanyAndModel(company, newModel))
                .thenReturn(false);
        when(restrictionRepository.save(any(Restriction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        RestrictionResponse resp = restrictionService.updateRestriction(
                company.getId(), restriction.getId(), updateReq
        );

        // Assert
        assertEquals(restriction.getId(), resp.getId());
        assertEquals(newModelId, resp.getModelId());
        assertEquals(updateReq.getMaxRequestsPerWindow(), resp.getMaxRequestsPerWindow());
        assertEquals(updateReq.getMaxTokensPerWindow(), resp.getMaxTokensPerWindow());
        assertEquals(updateReq.getWindowType(), resp.getWindowType());

        verify(restrictionRepository).save(restriction);
    }

    @Test
    void updateRestrictionThrowsWhenBelongsToAnotherCompany() {
        Company other = new Company();
        other.setId(99L);
        restriction.setCompany(other);

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.updateRestriction(company.getId(), restriction.getId(), request)
        );
        assertEquals("Restriction does not belong to the company", ex.getMessage());
    }

    @Test
    void deleteRestrictionSuccess() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));

        // Act
        restrictionService.deleteRestriction(company.getId(), restriction.getId());

        // Assert: se eliminó del repositorio
        verify(restrictionRepository).delete(restriction);
    }

    @Test
    void deleteRestrictionThrowsWhenBelongsToAnotherCompany() {
        Company other = new Company();
        other.setId(99L);
        restriction.setCompany(other);

        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.of(restriction));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.deleteRestriction(company.getId(), restriction.getId())
        );
        assertEquals("Restriction does not belong to the company", ex.getMessage());
    }

    @Test
    void deleteRestrictionThrowsWhenNotFound() {
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(restrictionRepository.findById(restriction.getId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                restrictionService.deleteRestriction(company.getId(), restriction.getId())
        );
        assertEquals("Restriction not found", ex.getMessage());
    }
}
