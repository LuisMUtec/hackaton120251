package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {
    List<Restriction> findByCompany(Company company);
    List<Restriction> findByModel(AIModel model);
    Optional<Restriction> findByCompanyAndModel(Company company, AIModel model);
    boolean existsByCompanyAndModel(Company company, AIModel model);
}