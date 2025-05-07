package com.example.sparkyaisystem.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_models")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String provider; // OpenAI, Meta, DeepSpeak, etc.

    @Column(nullable = false)
    private String type; // chat, completion, multimodal

    @Column(nullable = false)
    private boolean active;

    @Column
    private String description;
}