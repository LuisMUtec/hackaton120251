package com.example.sparkyaisystem.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private AIModel model;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String query;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column
    private String errorMessage;

    @Column(nullable = false)
    private boolean successful;

    @Column(nullable = false)
    private int tokensConsumed;

    @Column
    private String fileName; // For multimodal requests

    @Column(nullable = false)
    private LocalDateTime requestTime;

    @Column
    private LocalDateTime responseTime;

    @PrePersist
    protected void onCreate() {
        requestTime = LocalDateTime.now();
    }
}