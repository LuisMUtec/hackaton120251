package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIModelRepository extends JpaRepository<AIModel, Long> {
    Optional<AIModel> findByName(String name);
    List<AIModel> findByProvider(String provider);
    List<AIModel> findByType(String type);
    List<AIModel> findByActive(boolean active);
    boolean existsByName(String name);
}