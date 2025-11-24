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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultadosControllerTest {

    @Mock
    private EligeRepository eligeRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private ResultadosController controller;

    @Test
    void muestraMensajeCuandoNoHayVotos() {
        Model model = new ExtendedModelMap();
        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE")).thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE")).thenReturn(0L);
        when(eligeRepository.count()).thenReturn(0L);
        String vista = controller.mostrarResultados(model);

        assertThat(vista).isEqualTo("resultados");
        assertThat(model.getAttribute("mensaje")).isEqualTo("Aún no existen votos registrados.");
        assertThat(model.getAttribute("totalVotosEmitidos")).isEqualTo(0L);
        assertThat(model.getAttribute("resultados")).asList().isEmpty();
    }

    @Test
    void calculaResumenYParticipacionConVotos() {
        Model model = new ExtendedModelMap();
        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE")).thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE")).thenReturn(6L);
        when(eligeRepository.count()).thenReturn(6L);

        List<Object[]> resultados = List.of(new Object[]{1L, "Ana", 4L}, new Object[]{2L, "Luis", 2L});
        when(eligeRepository.contarVotosPorCandidato()).thenReturn(resultados);

        String vista = controller.mostrarResultados(model);

        assertThat(vista).isEqualTo("resultados");
        assertThat(model.getAttribute("sumaVotosCandidatos")).isEqualTo(6L);
        assertThat(model.getAttribute("porcentajeParticipacion")).isEqualTo(60.0);
        assertThat(model.getAttribute("verificacionOk")).isEqualTo(true);
        assertThat(model.getAttribute("resultados")).isSameAs(resultados);
    }
}
