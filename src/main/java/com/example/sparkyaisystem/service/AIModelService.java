package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.aimodel.AIModelResponse;
import com.example.sparkyaisystem.model.entity.*;
import com.example.sparkyaisystem.repository.*;
import com.example.sparkyaisystem.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIModelService {

    private final AIModelRepository aiModelRepository;
    private final RestrictionRepository restrictionRepository;
    private final OpenAiService openAiService;
    private final MetaService metaService;
    private final DeepSpeakService deepSpeakService;

    public AIModelService(
            AIModelRepository aiModelRepository,
            RestrictionRepository restrictionRepository,
            OpenAiService openAiService,
            MetaService metaService,
            DeepSpeakService deepSpeakService
    ) {
        this.aiModelRepository = aiModelRepository;
        this.restrictionRepository = restrictionRepository;
        this.openAiService = openAiService;
        this.metaService = metaService;
        this.deepSpeakService = deepSpeakService;
    }

    public String callModel(AIModel model, String prompt) {
        String response;

        if ("OpenAI".equalsIgnoreCase(model.getProvider())) {
            response = openAiService.callOpenAi(model, prompt);
        } else if ("Meta".equalsIgnoreCase(model.getProvider())) {
            response = metaService.callMeta(model, prompt);
        } else if ("DeepSpeak".equalsIgnoreCase(model.getProvider())) {
            response = deepSpeakService.callDeepSpeak(model, prompt);
        } else {
            throw new IllegalArgumentException("Proveedor no soportado: " + model.getProvider());
        }

        return response;
    }

    public List<AIModelResponse> getAvailableModelsForUser(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new RuntimeException("User does not belong to a company");
        }

        List<Restriction> restrictions = restrictionRepository.findByCompany(company);
        List<AIModel> activeModels = aiModelRepository.findByActive(true);

        return activeModels.stream()
                .filter(model -> restrictions.stream()
                        .anyMatch(r -> r.getModel().getId().equals(model.getId())))
                .map(this::mapToAIModelResponse)
                .collect(Collectors.toList());
    }

    public List<AIModelResponse> getActiveModels() {
        return aiModelRepository.findByActive(true).stream()
                .map(this::mapToAIModelResponse)
                .collect(Collectors.toList());
    }

    public boolean isModelAvailableForUser(User user, AIModel model) {
        if (!model.isActive()) {
            return false;
        }

        Company company = user.getCompany();
        if (company == null) {
            return false;
        }

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
                .available(model.isActive())
                .maxTokensPerRequest(getMaxTokensPerRequest(model.getProvider(), model.getType()))
                .build();
    }

    private Integer getMaxTokensPerRequest(String provider, String type) {
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

        return 1000;
    }
}
