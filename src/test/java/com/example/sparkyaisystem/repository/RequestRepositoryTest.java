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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestRepositoryMockitoTest {

    @Mock
    private RequestRepository repo;

    @Test
    @DisplayName("getTotalTokensConsumedByCompany suma tokens")
    void whenGetTotalTokensConsumedByCompany_thenReturnSum() {
        Company c = new Company(1L, "C","R", LocalDateTime.now(), true, null, null, null, null, null);
        when(repo.getTotalTokensConsumedByCompany(c)).thenReturn(123);

        Integer total = repo.getTotalTokensConsumedByCompany(c);

        assertThat(total).isEqualTo(123);
        verify(repo).getTotalTokensConsumedByCompany(c);
    }

    @Test
    @DisplayName("countRequestsByUserInTimeWindow cuenta correctamente")
    void whenCountRequestsByUserInTimeWindow_thenReturnCount() {
        User u = new User(2L, "A","B","a@b.com","pwd", Role.ROLE_USER, null, null, null);
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end   = LocalDateTime.now();
        when(repo.countRequestsByUserInTimeWindow(u, start, end)).thenReturn(7);

        Integer count = repo.countRequestsByUserInTimeWindow(u, start, end);

        assertThat(count).isEqualTo(7);
        verify(repo).countRequestsByUserInTimeWindow(u, start, end);
    }
}
