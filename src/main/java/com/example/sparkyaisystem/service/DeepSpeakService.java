package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.springframework.stereotype.Service;

@Service
public class DeepSpeakService {
    public String callDeepSpeak(AIModel model, String prompt) {
        return "Respuesta simulada de DeepSpeak para prompt: " + prompt;
    }
}
