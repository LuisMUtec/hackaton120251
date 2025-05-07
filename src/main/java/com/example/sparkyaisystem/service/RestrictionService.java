package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.restriction.RestrictionRequest;
import com.example.sparkyaisystem.model.dto.restriction.RestrictionResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestrictionService {

    private final RestrictionRepository restrictionRepository;
    private final CompanyRepository companyRepository;
    private final AIModelRepository aiModelRepository;

    public RestrictionService(RestrictionRepository restrictionRepository,
                              CompanyRepository companyRepository,
                              AIModelRepository aiModelRepository) {
        this.restrictionRepository = restrictionRepository;
        this.companyRepository = companyRepository;
        this.aiModelRepository = aiModelRepository;
    }

    @Transactional
    public RestrictionResponse createRestriction(Long companyId, RestrictionRequest restrictionRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        AIModel model = aiModelRepository.findById(restrictionRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if restriction already exists for this company and model
        if (restrictionRepository.existsByCompanyAndModel(company, model)) {
            throw new RuntimeException("Restriction already exists for this company and model");
        }

        Restriction restriction = new Restriction();
        restriction.setCompany(company);
        restriction.setModel(model);
        restriction.setMaxRequestsPerWindow(restrictionRequest.getMaxRequestsPerWindow());
        restriction.setMaxTokensPerWindow(restrictionRequest.getMaxTokensPerWindow());
        restriction.setWindowType(restrictionRequest.getWindowType());

        company.addRestriction(restriction);
        
        return mapToRestrictionResponse(restrictionRepository.save(restriction));
    }

    public List<RestrictionResponse> getRestrictionsByCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return restrictionRepository.findByCompany(company).stream()
                .map(this::mapToRestrictionResponse)
                .collect(Collectors.toList());
    }

    public RestrictionResponse getRestrictionById(Long companyId, Long restrictionId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Restriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new RuntimeException("Restriction not found"));

        // Ensure the restriction belongs to the company
        if (!restriction.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Restriction does not belong to the company");
        }

        return mapToRestrictionResponse(restriction);
    }

    @Transactional
    public RestrictionResponse updateRestriction(Long companyId, Long restrictionId, RestrictionRequest restrictionRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Restriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new RuntimeException("Restriction not found"));

        // Ensure the restriction belongs to the company
        if (!restriction.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Restriction does not belong to the company");
        }

        AIModel model = aiModelRepository.findById(restrictionRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // If model is being changed, check if a restriction already exists for the new model
        if (!restriction.getModel().getId().equals(model.getId()) &&
                restrictionRepository.existsByCompanyAndModel(company, model)) {
            throw new RuntimeException("Restriction already exists for this company and model");
        }

        restriction.setModel(model);
        restriction.setMaxRequestsPerWindow(restrictionRequest.getMaxRequestsPerWindow());
        restriction.setMaxTokensPerWindow(restrictionRequest.getMaxTokensPerWindow());
        restriction.setWindowType(restrictionRequest.getWindowType());

        return mapToRestrictionResponse(restrictionRepository.save(restriction));
    }

    @Transactional
    public void deleteRestriction(Long companyId, Long restrictionId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Restriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new RuntimeException("Restriction not found"));

        // Ensure the restriction belongs to the company
        if (!restriction.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Restriction does not belong to the company");
        }

        company.removeRestriction(restriction);
        restrictionRepository.delete(restriction);
    }

    private RestrictionResponse mapToRestrictionResponse(Restriction restriction) {
        return RestrictionResponse.builder()
                .id(restriction.getId())
                .companyId(restriction.getCompany().getId())
                .companyName(restriction.getCompany().getName())
                .modelId(restriction.getModel().getId())
                .modelName(restriction.getModel().getName())
                .modelProvider(restriction.getModel().getProvider())
                .modelType(restriction.getModel().getType())
                .maxRequestsPerWindow(restriction.getMaxRequestsPerWindow())
                .maxTokensPerWindow(restriction.getMaxTokensPerWindow())
                .windowType(restriction.getWindowType())
                .createdAt(restriction.getCreatedAt())
                .updatedAt(restriction.getUpdatedAt())
                .build();
    }
}