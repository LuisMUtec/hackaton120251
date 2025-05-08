package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIModelRepositoryMockitoTest {

    @Mock
    private AIModelRepository repo;

    @Test
    @DisplayName("findByName devuelve el modelo simulado")
    void whenFindByName_thenReturnModel() {
        AIModel m = new AIModel(1L, "Test", "OpenAI", "chat", true, "desc");
        when(repo.findByName("Test")).thenReturn(Optional.of(m));

        Optional<AIModel> result = repo.findByName("Test");

        assertThat(result).isPresent().get().isEqualTo(m);
        verify(repo).findByName("Test");
    }

    @Test
    @DisplayName("existsByName confirma existencia")
    void whenExistsByName_thenTrue() {
        when(repo.existsByName("Test")).thenReturn(true);

        boolean exists = repo.existsByName("Test");

        assertThat(exists).isTrue();
        verify(repo).existsByName("Test");
    }
}