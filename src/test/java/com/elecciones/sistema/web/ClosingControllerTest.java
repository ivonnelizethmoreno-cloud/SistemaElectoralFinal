package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.UserAccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosingControllerTest {

    @Mock
    private EligeRepository eligeRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private ResultadosController resultadosController;

    // ===========================================================
    // Caso 1: No existen votos
    // ===========================================================
    @Test
    void mostrarResultadosSinVotos() {
        Model model = new ExtendedModelMap();

        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE"))
                .thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE"))
                .thenReturn(0L);
        when(eligeRepository.count())
                .thenReturn(0L);

        String vista = resultadosController.mostrarResultados(model);

        assertThat(vista).isEqualTo("resultados");
        assertThat(model.getAttribute("mensaje"))
                .isEqualTo("Aún no existen votos registrados.");
        assertThat(model.getAttribute("totalVotosEmitidos")).isEqualTo(0);

        assertThat(model.getAttribute("resultados"))
                .isEqualTo(Collections.emptyList());
    }

    // ===========================================================
    // Caso 2: Existen votos y se procesan correctamente
    // ===========================================================
    @Test
    void mostrarResultadosConVotos() {
        Model model = new ExtendedModelMap();

        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE"))
                .thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE"))
                .thenReturn(5L);
        when(eligeRepository.count())
                .thenReturn(5L);

        // Resultado simulado: [cedula, nombre, votos]
        List<Object[]> resultadosSimulados = List.of(
                new Object[]{"123", "Juan Pérez", 3L},
                new Object[]{"456", "Ana Díaz", 2L}
        );

        when(eligeRepository.contarVotosPorCandidato())
                .thenReturn(resultadosSimulados);

        String vista = resultadosController.mostrarResultados(model);

        assertThat(vista).isEqualTo("resultados");
        assertThat(model.getAttribute("totalVotosEmitidos")).isEqualTo(5L);
        assertThat(model.getAttribute("sumaVotosCandidatos")).isEqualTo(5L);
        assertThat(model.getAttribute("porcentajeParticipacion"))
                .isEqualTo(50.0); // 5 de 10 votantes
        assertThat(model.getAttribute("resultados"))
                .isEqualTo(resultadosSimulados);
    }
}
