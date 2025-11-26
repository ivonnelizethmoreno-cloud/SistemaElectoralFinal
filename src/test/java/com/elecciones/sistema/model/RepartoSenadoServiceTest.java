package com.elecciones.sistema.service;

import com.elecciones.sistema.repo.EligeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RepartoSenadoServiceTest {

    private EligeRepository eligeRepository;
    private RepartoSenadoService service;

    @BeforeEach
    void setUp() {
        eligeRepository = Mockito.mock(EligeRepository.class);
        service = new RepartoSenadoService(eligeRepository);
    }

    @Test
    void testCalcularCurulesSenado_ComportamientoCompleto() {

        // 🔹 TOTAL VOTOS
        when(eligeRepository.count()).thenReturn(1000L);

        // 🔹 VOTOS POR PARTIDO (nombre, votos)
        List<Object[]> resultados = Arrays.asList(
                new Object[]{"A", 400},
                new Object[]{"B", 300},
                new Object[]{"C", 200},
                new Object[]{"D", 50}        // <— SÍ supera el umbral (30)
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(resultados);

        // 🔹 Ejecutar método
        Map<String, Object> respuesta = service.calcularCurulesSenado();

        // Total de votos
        assertThat(respuesta.get("totalVotos")).isEqualTo(1000L);

        // Umbral = 3% de 1000 = 30
        assertThat(respuesta.get("umbral")).isEqualTo(30.0);

        // Cifra repartidora válida
        double cifraRepartidora = (double) respuesta.get("cifraRepartidora");
        assertThat(cifraRepartidora).isGreaterThan(0);

        Map<String, Integer> curules = (Map<String, Integer>) respuesta.get("curules");

        // 🔹 Todos los partidos A, B, C y D superan el umbral
        assertThat(curules).containsKeys("A", "B", "C", "D");

        // Cada partido debe tener al menos 1 curul
        curules.forEach((k, v) ->
                assertThat(v).withFailMessage(k + " debe tener al menos 1 curul").isGreaterThan(0)
        );

        // Total curules = EXACTAMENTE 100
        int totalAsignado = curules.values().stream().mapToInt(i -> i).sum();
        assertThat(totalAsignado).isEqualTo(100);

        // Verificar llamadas al repositorio
        verify(eligeRepository, times(1)).count();
        verify(eligeRepository, times(1)).contarVotosPorPartido();
    }

    @Test
    void testCalcularCurulesSenado_SinPartidosValidos() {

        // 🔹 TOTAL votos = 1000
        when(eligeRepository.count()).thenReturn(1000L);

        // 🔹 Todos los partidos debajo del umbral
        List<Object[]> resultados = Arrays.asList(
                new Object[]{"X", 10},
                new Object[]{"Y", 20}
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(resultados);

        Map<String, Object> respuesta = service.calcularCurulesSenado();

        Map<String, Integer> curules = (Map<String, Integer>) respuesta.get("curules");

        // 🔹 No hay partidos válidos
        assertThat(curules).isEmpty();

        // 🔹 Cifra repartidora debe ser 0 cuando no hay cocientes
        assertThat((double) respuesta.get("cifraRepartidora")).isEqualTo(0.0);

        verify(eligeRepository, times(1)).count();
        verify(eligeRepository, times(1)).contarVotosPorPartido();
    }
}
