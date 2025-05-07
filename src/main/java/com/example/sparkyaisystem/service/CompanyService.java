package com.example.sparkyaisystem.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final RequestRepository requestRepository;
    private final AuthService authService;

    public CompanyService(CompanyRepository companyRepository, RequestRepository requestRepository, AuthService authService) {
        this.companyRepository = companyRepository;
        this.requestRepository = requestRepository;
        this.authService = authService;
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest companyRequest) {
        // Check if company with same name or RUC already exists
        if (companyRepository.existsByName(companyRequest.getName())) {
            throw new RuntimeException("Company with this name already exists");
        }
        if (companyRepository.existsByRuc(companyRequest.getRuc())) {
            throw new RuntimeException("Company with this RUC already exists");
        }

        // Create company
        Company company = new Company();
        company.setName(companyRequest.getName());
        company.setRuc(companyRequest.getRuc());
        company.setActive(companyRequest.isActive());
        company.setAffiliationDate(LocalDateTime.now());
        
        // Save company to get ID
        company = companyRepository.save(company);
        
        // Create admin user for the company
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setFirstName(companyRequest.getAdminFirstName());
        adminRequest.setLastName(companyRequest.getAdminLastName());
        adminRequest.setEmail(companyRequest.getAdminEmail());
        adminRequest.setPassword(companyRequest.getAdminPassword());
        
        User admin = authService.registerCompanyAdmin(adminRequest, company);
        
        // Set admin for the company
        company.setAdmin(admin);
        company = companyRepository.save(company);
        
        return mapToCompanyResponse(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToCompanyResponse)
                .collect(Collectors.toList());
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToCompanyResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest companyRequest) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Check if name is being changed and if it's already taken
        if (!company.getName().equals(companyRequest.getName()) && 
                companyRepository.existsByName(companyRequest.getName())) {
            throw new RuntimeException("Company with this name already exists");
        }
        
        // Check if RUC is being changed and if it's already taken
        if (!company.getRuc().equals(companyRequest.getRuc()) && 
                companyRepository.existsByRuc(companyRequest.getRuc())) {
            throw new RuntimeException("Company with this RUC already exists");
        }
        
        company.setName(companyRequest.getName());
        company.setRuc(companyRequest.getRuc());
        company.setActive(companyRequest.isActive());
        
        return mapToCompanyResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponse updateCompanyStatus(Long id, CompanyStatusRequest statusRequest) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setActive(statusRequest.getActive());
        
        return mapToCompanyResponse(companyRepository.save(company));
    }

    public CompanyConsumptionResponse getCompanyConsumption(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        List<Request> requests = requestRepository.findByCompany(company);
        
        int totalRequests = requests.size();
        int totalTokensConsumed = 0;
        
        Map<AIModel, ModelConsumptionData> modelConsumptionMap = new HashMap<>();
        
        for (Request request : requests) {
            totalTokensConsumed += request.getTokensConsumed();
            
            AIModel model = request.getModel();
            ModelConsumptionData consumptionData = modelConsumptionMap.getOrDefault(model, new ModelConsumptionData(model));
            consumptionData.requestsCount++;
            consumptionData.tokensConsumed += request.getTokensConsumed();
            modelConsumptionMap.put(model, consumptionData);
        }
        
        List<CompanyConsumptionResponse.ModelConsumption> modelConsumptions = modelConsumptionMap.values().stream()
                .map(data -> CompanyConsumptionResponse.ModelConsumption.builder()
                        .modelId(data.model.getId())
                        .modelName(data.model.getName())
                        .modelProvider(data.model.getProvider())
                        .requestsCount(data.requestsCount)
                        .tokensConsumed(data.tokensConsumed)
                        .build())
                .collect(Collectors.toList());
        
        return CompanyConsumptionResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .totalRequests(totalRequests)
                .totalTokensConsumed(totalTokensConsumed)
                .modelConsumptions(modelConsumptions)
                .build();
    }

    private CompanyResponse mapToCompanyResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .ruc(company.getRuc())
                .affiliationDate(company.getAffiliationDate())
                .active(company.isActive())
                .adminId(company.getAdmin() != null ? company.getAdmin().getId() : null)
                .adminName(company.getAdmin() != null ? 
                        company.getAdmin().getFirstName() + " " + company.getAdmin().getLastName() : null)
                .adminEmail(company.getAdmin() != null ? company.getAdmin().getEmail() : null)
                .totalUsers(company.getUsers().size())
                .totalRestrictions(company.getRestrictions().size())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    // Helper class for aggregating model consumption data
    private static class ModelConsumptionData {
        private final AIModel model;
        private int requestsCount;
        private int tokensConsumed;

        public ModelConsumptionData(AIModel model) {
            this.model = model;
            this.requestsCount = 0;
            this.tokensConsumed = 0;
        }
    }
}