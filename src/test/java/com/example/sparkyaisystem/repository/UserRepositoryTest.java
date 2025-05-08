package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryMockitoTest {

    @Mock
    private UserRepository repo;

    @Test
    @DisplayName("findByEmail devuelve usuario")
    void whenFindByEmail_thenReturnUser() {
        User u = new User(1L, "X","Y","x@y.com","pwd", Role.ROLE_USER, null, null, null);
        when(repo.findByEmail("x@y.com")).thenReturn(Optional.of(u));

        Optional<User> op = repo.findByEmail("x@y.com");

        assertThat(op).isPresent().get().isEqualTo(u);
        verify(repo).findByEmail("x@y.com");
    }

    @Test
    @DisplayName("findByCompanyAndRole filtra correctamente")
    void whenFindByCompanyAndRole_thenReturnList() {
        Company c = new Company(3L, "Co","R", LocalDateTime.now(), true, null, null, null, null, null);
        User u1 = new User(4L, "A","B","a@b.com","pwd", Role.ROLE_COMPANY_ADMIN, c, null, null);
        when(repo.findByCompanyAndRole(c, Role.ROLE_COMPANY_ADMIN)).thenReturn(List.of(u1));

        var list = repo.findByCompanyAndRole(c, Role.ROLE_COMPANY_ADMIN);

        assertThat(list).hasSize(1).containsExactly(u1);
        verify(repo).findByCompanyAndRole(c, Role.ROLE_COMPANY_ADMIN);
    }
}