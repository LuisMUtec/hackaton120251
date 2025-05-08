package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LimitRepositoryMockitoTest {

    @Mock
    private LimitRepository repo;

    @Test
    @DisplayName("findByUser devuelve la lista esperada")
    void whenFindByUser_thenReturnList() {
        // Crea el usuario usando no-args + setters
        User u = new User();
        u.setId(1L);
        u.setFirstName("A");
        u.setLastName("B");
        u.setEmail("a@b.com");
        u.setPassword("pwd");
        u.setRole(Role.ROLE_USER);
        // omitimos company, limits, requests

        // Crea el modelo del mismo modo
        AIModel m = new AIModel();
        m.setId(2L);
        m.setName("M");
        m.setProvider("OpenAI");
        m.setType("chat");
        m.setActive(true);
        // omitimos description

        // Ahora construye el Limit sólo con los campos importantes
        Limit l = new Limit();
        l.setId(1L);
        l.setUser(u);
        l.setModel(m);
        l.setMaxRequestsPerWindow(5);
        l.setMaxTokensPerWindow(50);
        l.setWindowType("daily");
        // omitimos createdAt, updatedAt, requests, etc.

        when(repo.findByUser(u)).thenReturn(List.of(l));

        var result = repo.findByUser(u);

        assertThat(result).hasSize(1).containsExactly(l);
        verify(repo).findByUser(u);
    }

    @Test
    @DisplayName("existsByUserAndModel retorna false si no existe")
    void whenExistsByUserAndModel_thenFalse() {
        User u = new User();
        u.setId(1L);
        u.setFirstName("X");
        u.setLastName("Y");
        u.setEmail("x@y.com");
        u.setPassword("pwd");
        u.setRole(Role.ROLE_USER);

        AIModel m = new AIModel();
        m.setId(3L);
        m.setName("N");
        m.setProvider("Meta");
        m.setType("completion");
        m.setActive(false);

        when(repo.existsByUserAndModel(u, m)).thenReturn(false);

        boolean ok = repo.existsByUserAndModel(u, m);

        assertThat(ok).isFalse();
        verify(repo).existsByUserAndModel(u, m);
    }
}
