package com.example.sparkyaisystem.controller;

import com.example.sparkyaisystem.model.dto.company.CompanyConsumptionResponse;
import com.example.sparkyaisystem.model.dto.company.CompanyRequest;
import com.example.sparkyaisystem.model.dto.company.CompanyResponse;
import com.example.sparkyaisystem.model.dto.company.CompanyStatusRequest;
import com.example.sparkyaisystem.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
@PreAuthorize("hasRole('SPARKY_ADMIN')")
public class AdminController {

    private final CompanyService companyService;

    public AdminController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest companyRequest) {
        CompanyResponse companyResponse = companyService.createCompany(companyRequest);
        return ResponseEntity.ok(companyResponse);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<CompanyResponse> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        CompanyResponse companyResponse = companyService.getCompanyById(id);
        return ResponseEntity.ok(companyResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest companyRequest) {
        CompanyResponse companyResponse = companyService.updateCompany(id, companyRequest);
        return ResponseEntity.ok(companyResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CompanyResponse> updateCompanyStatus(
            @PathVariable Long id,
            @Valid @RequestBody CompanyStatusRequest statusRequest) {
        CompanyResponse companyResponse = companyService.updateCompanyStatus(id, statusRequest);
        return ResponseEntity.ok(companyResponse);
    }

    @GetMapping("/{id}/consumption")
    public ResponseEntity<CompanyConsumptionResponse> getCompanyConsumption(@PathVariable Long id) {
        CompanyConsumptionResponse consumptionResponse = companyService.getCompanyConsumption(id);
        return ResponseEntity.ok(consumptionResponse);
    }
}