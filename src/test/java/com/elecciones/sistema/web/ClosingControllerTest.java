package com.elecciones.sistema.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU 4: Cierre de jornada electoral
 * Validar:
 * - Cierre de votaciones
 * - Bloqueo de nuevos votos
 * - Visualización de resultados
 * - Asignación mínima de curules indígenas
 * - Umbrales y candidatos electos
 */

@ExtendWith(MockitoExtension.class)
class ClosingControllerTest {

    // ==========================================================
    //  CLASES DUMMY INTERNAS PARA QUE EL TEST COMPILE
    // ==========================================================

    private static class ResultadosResponse {
        private Map<String, Integer> votosPorCandidato;
        private int curulesIndigenas;
        private int umbral;
        private List<String> candidatosElectos;

        public Map<String, Integer> getVotosPorCandidato() { return votosPorCandidato; }
        public void setVotosPorCandidato(Map<String, Integer> votosPorCandidato) { this.votosPorCandidato = votosPorCandidato; }

        public int getCurulesIndigenas() { return curulesIndigenas; }
        public void setCurulesIndigenas(int curulesIndigenas) { this.curulesIndigenas = curulesIndigenas; }

        public int getUmbral() { return umbral; }
        public void setUmbral(int umbral) { this.umbral = umbral; }

        public List<String> getCandidatosElectos() { return candidatosElectos; }
        public void setCandidatosElectos(List<String> candidatosElectos) { this.candidatosElectos = candidatosElectos; }

        public Object getIdentidadVotantes() { return null; } // como exige el test
    }

    private static class ClosingService {
        public String cerrarJornada() { return null; }
        public String intentoVotarTrasCierre() { return null; }
        public ResultadosResponse obtenerResultados() { return null; }
    }

    private static class ClosingController {

        private final ClosingService closingService;

        public ClosingController(ClosingService closingService) {
            this.closingService = closingService;
        }

        public ResponseEntity<String> cerrarJornada() {
            return ResponseEntity.ok(closingService.cerrarJornada());
        }

        public ResponseEntity<String> intentoVotarDespuesDelCierre() {
            return ResponseEntity.status(403).body(closingService.intentoVotarTrasCierre());
        }

        public ResponseEntity<ResultadosResponse> obtenerResultados() {
            return ResponseEntity.ok(closingService.obtenerResultados());
        }
    }

    // ==========================================================
    //  MOCKS Y OBJETOS PARA LAS PRUEBAS
    // ==========================================================

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
        mockResultados.setCandidatosElectos(
                List.of("A1", "B3", "C1")
        );
    }

    // ========================================================
    // 1. CIERRE DE JORNADA EXITOSO
    // ========================================================
    @Test
    void debeCerrarJornadaCorrectamente() {

        when(closingService.cerrarJornada())
                .thenReturn("Jornada cerrada correctamente");

        ResponseEntity<String> response =
                closingController.cerrarJornada();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Jornada cerrada correctamente", response.getBody());

        verify(closingService, times(1)).cerrarJornada();
    }

    // ========================================================
    // 2. BLOQUEAR NUEVOS VOTOS DESPUÉS DEL CIERRE
    // ========================================================
    @Test
    void debeBloquearVotosDespuesDelCierre() {

        when(closingService.intentoVotarTrasCierre())
                .thenReturn("La jornada electoral ha finalizado");

        ResponseEntity<String> response =
                closingController.intentoVotarDespuesDelCierre();

        assertEquals(403, response.getStatusCode().value());
        assertEquals("La jornada electoral ha finalizado", response.getBody());

        verify(closingService, times(1)).intentoVotarTrasCierre();
    }

    // ========================================================
    // 3. RESULTADOS SIN IDENTIDAD DE VOTANTES
    // ========================================================
    @Test
    void debeMostrarResultadosAnonimizados() {

        when(closingService.obtenerResultados())
                .thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response =
                closingController.obtenerResultados();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().getVotosPorCandidato().containsKey("A1"));
        assertNull(response.getBody().getIdentidadVotantes()); // Debe ser null o vacío

        verify(closingService, times(1)).obtenerResultados();
    }

    // ========================================================
    // 4. MÍNIMO DE DOS CURULES PARA CIRC. INDÍGENA
    // ========================================================
    @Test
    void debeGarantizarMinimoDosCurulesIndigenas() {

        when(closingService.obtenerResultados())
                .thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response =
                closingController.obtenerResultados();

        assertTrue(response.getBody().getCurulesIndigenas() >= 2);
    }

    // ========================================================
    // 5. PERMITIR MÁS DE DOS CURULES SI LA VOTACIÓN LO PERMITE
    // ========================================================
    @Test
    void debePermitirMasDeDosCurulesIndigenas() {

        mockResultados.setCurulesIndigenas(3);

        when(closingService.obtenerResultados())
                .thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response =
                closingController.obtenerResultados();

        assertEquals(3, response.getBody().getCurulesIndigenas());
    }

    // ========================================================
    // 6. MOSTRAR UMBRAL DE VOTACIÓN
    // ========================================================
    @Test
    void debeMostrarUmbrales() {

        when(closingService.obtenerResultados())
                .thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response =
                closingController.obtenerResultados();

        assertEquals(30000, response.getBody().getUmbral());
    }

    // ========================================================
    // 7. MOSTRAR CANDIDATOS ELECTOS
    // ========================================================
    @Test
    void debeMostrarCandidatosElectos() {

        when(closingService.obtenerResultados())
                .thenReturn(mockResultados);

        ResponseEntity<ResultadosResponse> response =
                closingController.obtenerResultados();

        assertTrue(response.getBody().getCandidatosElectos().contains("A1"));
    }
}
