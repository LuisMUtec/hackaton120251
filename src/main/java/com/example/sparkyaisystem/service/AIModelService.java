package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.aimodel.AIModelResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIModelService {

    private final AIModelRepository aiModelRepository;
    private final RestrictionRepository restrictionRepository;

    public AIModelService(AIModelRepository aiModelRepository, RestrictionRepository restrictionRepository) {
        this.aiModelRepository = aiModelRepository;
        this.restrictionRepository = restrictionRepository;
    }

    @Transactional
    public AIModelResponse createModel(AIModel model) {
        if (aiModelRepository.existsByName(model.getName())) {
            throw new RuntimeException("AI Model with this name already exists");
        }
        
        return mapToAIModelResponse(aiModelRepository.save(model));
    }

    public List<AIModelResponse> getAllModels() {
        return aiModelRepository.findAll().stream()
                .map(this::mapToAIModelResponse)
                .collect(Collectors.toList());
    }

    public List<AIModelResponse> getActiveModels() {
        return aiModelRepository.findByActive(true).stream()
                .map(this::mapToAIModelResponse)
                .collect(Collectors.toList());
    }

    public AIModelResponse getModelById(Long id) {
        AIModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Model not found"));
        
        return mapToAIModelResponse(model);
    }

    @Transactional
    public AIModelResponse updateModel(Long id, AIModel modelDetails) {
        AIModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Model not found"));
        
        // Check if name is being changed and if it's already taken
        if (!model.getName().equals(modelDetails.getName()) && 
                aiModelRepository.existsByName(modelDetails.getName())) {
            throw new RuntimeException("AI Model with this name already exists");
        }
        
        model.setName(modelDetails.getName());
        model.setProvider(modelDetails.getProvider());
        model.setType(modelDetails.getType());
        model.setActive(modelDetails.isActive());
        model.setDescription(modelDetails.getDescription());
        
        return mapToAIModelResponse(aiModelRepository.save(model));
    }

    @Transactional
    public AIModelResponse toggleModelStatus(Long id, boolean active) {
        AIModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Model not found"));
        
        model.setActive(active);
        
        return mapToAIModelResponse(aiModelRepository.save(model));
    }

    public List<AIModelResponse> getAvailableModelsForUser(User user) {
        // Get user's company
        Company company = user.getCompany();
        if (company == null) {
            throw new RuntimeException("User does not belong to a company");
        }
        
        // Get company restrictions
        List<Restriction> restrictions = restrictionRepository.findByCompany(company);
        
        // Get all active models
        List<AIModel> activeModels = aiModelRepository.findByActive(true);
        
        // Filter models based on company restrictions
        return activeModels.stream()
                .filter(model -> restrictions.stream()
                        .anyMatch(r -> r.getModel().getId().equals(model.getId())))
                .map(model -> {
                    AIModelResponse response = mapToAIModelResponse(model);
                    response.setAvailable(true);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public boolean isModelAvailableForUser(User user, AIModel model) {
        // Check if model is active
        if (!model.isActive()) {
            return false;
        }
        
        // Get user's company
        Company company = user.getCompany();
        if (company == null) {
            return false;
        }
        
        // Check if company has a restriction for this model
        return restrictionRepository.existsByCompanyAndModel(company, model);
    }

    private AIModelResponse mapToAIModelResponse(AIModel model) {
        return AIModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .provider(model.getProvider())
                .type(model.getType())
                .active(model.isActive())
                .description(model.getDescription())
                .available(model.isActive()) // By default, available if active
                .maxTokensPerRequest(getMaxTokensPerRequest(model.getProvider(), model.getType()))
                .build();
    }

    // Helper method to determine max tokens per request based on model provider and type
    private Integer getMaxTokensPerRequest(String provider, String type) {
        // These values would typically come from configuration or the actual model specs
        if ("OpenAI".equalsIgnoreCase(provider)) {
            if ("chat".equalsIgnoreCase(type)) {
                return 4096;
            } else if ("completion".equalsIgnoreCase(type)) {
                return 2048;
            } else if ("multimodal".equalsIgnoreCase(type)) {
                return 4096;
            }
        } else if ("Meta".equalsIgnoreCase(provider)) {
            return 2048;
        } else if ("DeepSpeak".equalsIgnoreCase(provider)) {
            return 1024;
        }
        
        // Default value
        return 1000;
    }
}