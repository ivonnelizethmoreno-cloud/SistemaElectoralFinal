package com.elecciones.sistema.web;

import com.elecciones.sistema.dto.ResultadosResponse;
import com.elecciones.sistema.service.ClosingService;
import com.elecciones.sistema.controller.ClosingController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosingControllerTest {

    @Mock
    private ClosingService closingService;

    @InjectMocks
    private ClosingController closingController;

    private ResultadosResponse mockResultados;

    @BeforeEach
    void setUp() {
        mockResultados = new ResultadosResponse();
        mockResultados.setVotosPorCandidato(Map.of(
                "A1", 1200,
                "A2", 800
        ));
        mockResultados.setCurulesIndigenas(2);
        mockResultados.setUmbral(30000);
        mockResultados.setCandidatosElectos(List.of("A1", "B3", "C1"));
    }

    // ========================================================
    // 1. Cierre de jornada exitoso
    // ========================================================
    @Test
    void debeCerrarJornadaCorrectamente() {

        when(closingService.cerrarJornada())
                .thenReturn("Jornada cerrada correctamente");

        ResponseEntity<String> response = closingController.cerrarJornada();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Jornada cerrada correctamente");

        verify(closingService, times(1)).cerrarJornada();
    }

    // ========================================================
    // 2. Bloquear votos después del cierre
    // ========================================================
    @Test
    void debeBloquearVotosDespuesDelCierre() {

        when(closingService.intentoVotarTrasCierre())
                .thenReturn("La jornada electoral ha finalizado");

        ResponseEntity<String> response = closingController.intentoVotarDespuesDelCierre();

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isEqualTo("La jornada electoral ha finalizado");

        verify(closingService, times(1)).intentoVotarTrasCierre();
    }

    // ========================================================
    // 3. Resultados sin identidad de votantes
    // ========================================================
    @Test
    void debeMostrarResultadosAnonimizados() {

        when(closingService.obtenerResultados()).thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response = closingController.obtenerResultados();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getVotosPorCandidato()).containsKey("A1");
        assertThat(response.getBody().getIdentidadVotantes()).isNull();

        verify(closingService, times(1)).obtenerResultados();
    }

    // ========================================================
    // 4. Mínimo dos curules indígenas
    // ========================================================
    @Test
    void debeGarantizarMinimoDosCurulesIndigenas() {

        when(closingService.obtenerResultados()).thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response = closingController.obtenerResultados();

        assertThat(response.getBody().getCurulesIndigenas()).isGreaterThanOrEqualTo(2);
    }

    // ========================================================
    // 5. Permitir más de dos curules si votación lo permite
    // ========================================================
    @Test
    void debePermitirMasDeDosCurulesIndigenas() {

        mockResultados.setCurulesIndigenas(3);
        when(closingService.obtenerResultados()).thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response = closingController.obtenerResultados();

        assertThat(response.getBody().getCurulesIndigenas()).isEqualTo(3);
    }

    // ========================================================
    // 6. Mostrar umbral
    // ========================================================
    @Test
    void debeMostrarUmbral() {

        when(closingService.obtenerResultados()).thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response = closingController.obtenerResultados();

        assertThat(response.getBody().getUmbral()).isEqualTo(30000);
    }

    // ========================================================
    // 7. Mostrar candidatos electos
    // ========================================================
    @Test
    void debeMostrarCandidatosElectos() {

        when(closingService.obtenerResultados()).thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response = closingController.obtenerResultados();

        assertThat(response.getBody().getCandidatosElectos()).contains("A1");
    }
}
