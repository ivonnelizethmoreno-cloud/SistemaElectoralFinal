package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.UserAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU 4 – Cierre de jornada electoral
 * 
 * Este test valida el comportamiento REAL implementado en la aplicación:
 *   - Mostrar resultados del cierre
 *   - Validar integridad de votos
 *   - Participación
 *   - Suma correcta de votos
 *   - Cuando no existen votos
 * 
 * Nota: aunque la HU menciona un "ClosingController", la aplicación implementa
 *       el cierre y resultados en ResultadosController. Este test se adapta
 *       al backend REAL, manteniendo el nombre requerido.
 */
@ExtendWith(MockitoExtension.class)
class ClosingControllerTest {

    @Mock
    private EligeRepository eligeRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private ResultadosController resultadosController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // 1. Cuando no existen votos → mensaje y tabla vacía
    // ============================================================
    @Test
    void debeMostrarMensajeCuandoNoHayVotos() {

        when(eligeRepository.count()).thenReturn(0L);
        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE")).thenReturn(100L);

        String vista = resultadosController.mostrarResultados(model);

        assertEquals("resultados", vista);
        assertEquals("Aún no existen votos registrados.", model.getAttribute("mensaje"));
        assertEquals(100L, model.getAttribute("totalVotantes"));
        assertEquals(0L, model.getAttribute("votantesEfectivos"));
        assertEquals(0L, model.getAttribute("totalVotosEmitidos"));
        assertEquals(0L, model.getAttribute("sumaVotosCandidatos"));
        assertEquals(0.0, model.getAttribute("porcentajeParticipacion"));
        assertTrue((Boolean) model.getAttribute("verificacionOk"));
    }

    // ============================================================
    // 2. Mostrar resultados cuando sí hay votos
    // ============================================================
    @Test
    void debeMostrarResultadosConVotos() {

        when(eligeRepository.count()).thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE")).thenReturn(100L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE")).thenReturn(10L);

        when(eligeRepository.contarVotosPorCandidato()).thenReturn(
                List.of(
                        new Object[]{"Candidato 1", "Partido X", 6L},
                        new Object[]{"Candidato 2", "Partido Y", 4L}
                )
        );

        String vista = resultadosController.mostrarResultados(model);

        assertEquals("resultados", vista);
        assertEquals(100L, model.getAttribute("totalVotantes"));
        assertEquals(10L, model.getAttribute("votantesEfectivos"));
        assertEquals(10L, model.getAttribute("totalVotosEmitidos"));
        assertEquals(10L, model.getAttribute("sumaVotosCandidatos"));
        assertEquals(10.0, (Double) model.getAttribute("porcentajeParticipacion"));
        assertTrue((Boolean) model.getAttribute("verificacionOk"));

        List<?> resultados = (List<?>) model.getAttribute("resultados");
        assertEquals(2, resultados.size());
    }

    // ============================================================
    // 3. Integridad electoral: votos inconsistentes
    // ============================================================
    @Test
    void verificacionDebeSerFalseSiNoCoincidenTotales() {

        when(eligeRepository.count()).thenReturn(10L);
        when(userAccountRepository.countByRoleIgnoreCase("VOTANTE")).thenReturn(100L);
        when(userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE")).thenReturn(8L);

        when(eligeRepository.contarVotosPorCandidato()).thenReturn(
                List.of(
                        new Object[]{"Candidato 1", "Partido X", 6L},
                        new Object[]{"Candidato 2", "Partido Y", 4L}
                )
        );

        resultadosController.mostrarResultados(model);

        assertFalse((Boolean) model.getAttribute("verificacionOk"));
    }
}

