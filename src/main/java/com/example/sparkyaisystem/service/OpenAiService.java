package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.springframework.stereotype.Service;

@Service
public class OpenAiService {
    public String callOpenAi(AIModel model, String prompt) {
        return "Respuesta simulada de OpenAI para prompt: " + prompt;
    }
}
