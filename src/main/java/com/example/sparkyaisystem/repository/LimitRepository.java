package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Long> {
    List<Limit> findByUser(User user);
    List<Limit> findByModel(AIModel model);
    Optional<Limit> findByUserAndModel(User user, AIModel model);
    List<Limit> findByUserAndWindowEndTimeBefore(User user, LocalDateTime dateTime);
    List<Limit> findByWindowType(String windowType);
    boolean existsByUserAndModel(User user, AIModel model);
}