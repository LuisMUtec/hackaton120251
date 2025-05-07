package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByCompany(Company company);
    List<User> findByCompanyAndRole(Company company, Role role);
    Optional<User> findByCompanyAndId(Company company, Long id);
}