package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.springframework.stereotype.Service;

@Service
public class MetaService {
    public String callMeta(AIModel model, String prompt) {
        return "Respuesta simulada de Meta para prompt: " + prompt;
    }
}
