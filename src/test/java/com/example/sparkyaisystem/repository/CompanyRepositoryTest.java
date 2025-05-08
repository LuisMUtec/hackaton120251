package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyRepositoryMockitoTest {

    @Mock
    private CompanyRepository repo;

    @Test
    @DisplayName("findByRuc devuelve vacío si no existe")
    void whenFindByRucNotFound_thenEmpty() {
        when(repo.findByRuc("NOEXIST")).thenReturn(Optional.empty());

        Optional<Company> op = repo.findByRuc("NOEXIST");

        assertThat(op).isEmpty();
        verify(repo).findByRuc("NOEXIST");
    }

    @Test
    @DisplayName("existsByName detecta nombre existente")
    void whenExistsByName_thenTrue() {
        when(repo.existsByName("Acme")).thenReturn(true);

        boolean ok = repo.existsByName("Acme");

        assertThat(ok).isTrue();
        verify(repo).existsByName("Acme");
    }
}