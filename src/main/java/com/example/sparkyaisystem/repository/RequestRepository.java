package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Request;
import com.example.sparkyaisystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUser(User user);
    List<Request> findByModel(AIModel model);
    List<Request> findByUserAndModel(User user, AIModel model);
    List<Request> findByUserAndRequestTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Request> findByModelAndRequestTimeBetween(AIModel model, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM Request r WHERE r.user.company = :company")
    List<Request> findByCompany(@Param("company") Company company);
    
    @Query("SELECT r FROM Request r WHERE r.user.company = :company AND r.requestTime BETWEEN :start AND :end")
    List<Request> findByCompanyAndRequestTimeBetween(
            @Param("company") Company company, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(r.tokensConsumed) FROM Request r WHERE r.user = :user")
    Integer getTotalTokensConsumedByUser(@Param("user") User user);
    
    @Query("SELECT SUM(r.tokensConsumed) FROM Request r WHERE r.user.company = :company")
    Integer getTotalTokensConsumedByCompany(@Param("company") Company company);
    
    @Query("SELECT COUNT(r) FROM Request r WHERE r.user = :user AND r.requestTime BETWEEN :start AND :end")
    Integer countRequestsByUserInTimeWindow(
            @Param("user") User user, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(r.tokensConsumed) FROM Request r WHERE r.user = :user AND r.requestTime BETWEEN :start AND :end")
    Integer getTotalTokensConsumedByUserInTimeWindow(
            @Param("user") User user, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
}