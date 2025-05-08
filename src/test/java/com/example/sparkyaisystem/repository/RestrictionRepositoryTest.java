package com.example.sparkyaisystem.repository;

import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Restriction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestrictionRepositoryMockitoTest {

    @Mock
    private RestrictionRepository repo;

    @Test
    @DisplayName("findByCompany devuelve restricciones")
    void whenFindByCompany_thenReturnList() {
        Company c = new Company(1L, "X","R", LocalDateTime.now(), true, null, null, null, null, null);
        AIModel m = new AIModel(2L, "M","DeepSpeak","multimodal",true,null);
        Restriction r = new Restriction(1L, c, m, 10, 100, "hourly", null, null);
        when(repo.findByCompany(c)).thenReturn(List.of(r));

        var list = repo.findByCompany(c);

        assertThat(list).hasSize(1).containsExactly(r);
        verify(repo).findByCompany(c);
    }

    @Test
    @DisplayName("existsByCompanyAndModel detecta correctamente")
    void whenExistsByCompanyAndModel_thenTrue() {
        Company c = new Company(1L, "Y","R2", LocalDateTime.now(), true, null, null, null, null, null);
        AIModel m = new AIModel(3L, "Z","Meta","completion",true,null);
        when(repo.existsByCompanyAndModel(c, m)).thenReturn(true);

        boolean ok = repo.existsByCompanyAndModel(c, m);

        assertThat(ok).isTrue();
        verify(repo).existsByCompanyAndModel(c, m);
    }
}