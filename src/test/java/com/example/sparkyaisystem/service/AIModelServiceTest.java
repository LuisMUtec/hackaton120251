package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.aimodel.AIModelResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIModelServiceTest {

    @Mock
    private AIModelRepository aiModelRepository;

    @Mock
    private RestrictionRepository restrictionRepository;

    @InjectMocks
    private AIModelService service;

    private AIModel sampleModel;
    private Company sampleCompany;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleModel = new AIModel();
        sampleModel.setId(1L);
        sampleModel.setName("TestModel");
        sampleModel.setProvider("OpenAI");
        sampleModel.setType("chat");
        sampleModel.setActive(true);
        sampleModel.setDescription("A test model");

        sampleCompany = new Company();
        sampleCompany.setId(100L);

        sampleUser = new User();
        sampleUser.setId(50L);
        sampleUser.setCompany(sampleCompany);
    }

    @Test
    void createModelSuccess() {
        // Arrange
        when(aiModelRepository.existsByName(eq(sampleModel.getName()))).thenReturn(false);
        when(aiModelRepository.save(any(AIModel.class))).thenReturn(sampleModel);

        // Act
        AIModelResponse response = service.createModel(sampleModel);

        // Assert
        assertNotNull(response);
        assertEquals(sampleModel.getId(), response.getId());
        verify(aiModelRepository).existsByName(sampleModel.getName());
        verify(aiModelRepository).save(sampleModel);
    }

    @Test
    void createModelThrowsWhenNameExists() {
        // Arrange
        when(aiModelRepository.existsByName(eq(sampleModel.getName()))).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.createModel(sampleModel));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(aiModelRepository).existsByName(sampleModel.getName());
        verify(aiModelRepository, never()).save(any(AIModel.class));
    }

    @Test
    void getAllModelsShouldReturnMappedResponses() {
        // Arrange
        when(aiModelRepository.findAll()).thenReturn(List.of(sampleModel));

        // Act
        List<AIModelResponse> results = service.getAllModels();

        // Assert
        assertEquals(1, results.size());
        assertEquals(sampleModel.getName(), results.get(0).getName());
        verify(aiModelRepository).findAll();
    }

    @Test
    void getActiveModelsShouldFilterByActiveFlag() {
        // Arrange
        when(aiModelRepository.findByActive(eq(true))).thenReturn(List.of(sampleModel));

        // Act
        List<AIModelResponse> results = service.getActiveModels();

        // Assert
        assertNotNull(results);
        assertTrue(results.stream().allMatch(AIModelResponse::isActive));
        verify(aiModelRepository).findByActive(true);
    }

    @Test
    void getModelByIdThrowsWhenNotFound() {
        // Arrange
        when(aiModelRepository.findById(eq(999L))).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.getModelById(999L));
        assertEquals("AI Model not found", exception.getMessage());
        verify(aiModelRepository).findById(999L);
    }

    @Test
    void toggleModelStatusShouldUpdateActiveFlag() {
        // Arrange
        when(aiModelRepository.findById(eq(sampleModel.getId()))).thenReturn(Optional.of(sampleModel));
        when(aiModelRepository.save(any(AIModel.class))).thenReturn(sampleModel);

        // Act
        AIModelResponse response = service.toggleModelStatus(sampleModel.getId(), false);

        // Assert
        assertFalse(response.isActive());
        verify(aiModelRepository).findById(sampleModel.getId());
        verify(aiModelRepository).save(sampleModel);
    }

    @Test
    void getAvailableModelsForUserShouldReturnRestrictedModels() {
        // Arrange
        Restriction restriction = new Restriction();
        restriction.setCompany(sampleCompany);
        restriction.setModel(sampleModel);

        when(restrictionRepository.findByCompany(eq(sampleCompany))).thenReturn(List.of(restriction));
        when(aiModelRepository.findByActive(eq(true))).thenReturn(List.of(sampleModel));

        // Act
        List<AIModelResponse> available = service.getAvailableModelsForUser(sampleUser);

        // Assert
        assertEquals(1, available.size());
        assertTrue(available.get(0).isAvailable());
        verify(restrictionRepository).findByCompany(sampleCompany);
        verify(aiModelRepository).findByActive(true);
    }

    @Test
    void isModelAvailableForUserShouldCheckExistence() {
        // Arrange
        when(restrictionRepository.existsByCompanyAndModel(eq(sampleCompany), eq(sampleModel)))
                .thenReturn(true);

        // Act
        boolean available = service.isModelAvailableForUser(sampleUser, sampleModel);

        // Assert
        assertTrue(available);
        verify(restrictionRepository).existsByCompanyAndModel(sampleCompany, sampleModel);
    }
}
