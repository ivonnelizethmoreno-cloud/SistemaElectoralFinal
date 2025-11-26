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

        // TOTAL VOTOS
        when(eligeRepository.count()).thenReturn(1000L);

        // (nombre, votos)
        List<Object[]> resultados = Arrays.asList(
                new Object[]{"A", 400},
                new Object[]{"B", 300},
                new Object[]{"C", 200},
                new Object[]{"D", 50}  // supera umbral (30)
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(resultados);

        Map<String, Object> respuesta = service.calcularCurulesSenado();

        // Total votos
        assertThat(respuesta.get("totalVotos")).isEqualTo(1000L);

        // Umbral = 3% = 30
        assertThat(respuesta.get("umbral")).isEqualTo(30.0);

        // Cifra repartidora > 0
        double cifraRepartidora = (double) respuesta.get("cifraRepartidora");
        assertThat(cifraRepartidora).isGreaterThan(0.0);

        Map<String, Integer> curules = (Map<String, Integer>) respuesta.get("curules");

        // Todos los partidos superan el umbral
        assertThat(curules).containsKeys("A", "B", "C", "D");

        // Cada partido recibe al menos un escaño (esperado por los votos configurados)
        curules.values().forEach(v ->
                assertThat(v).isGreaterThan(0)
        );

        // El número total de curules debe ser 100 (regla del Senado)
        int totalAsignado = curules.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(totalAsignado)
                .withFailMessage("La suma de curules debería ser 100, pero fue: " + totalAsignado)
                .isEqualTo(100);

        verify(eligeRepository, times(1)).count();
        verify(eligeRepository, times(1)).contarVotosPorPartido();
    }

    @Test
    void testCalcularCurulesSenado_SinPartidosValidos() {

        // Total votos
        when(eligeRepository.count()).thenReturn(1000L);

        // Todos los votos por debajo del umbral
        List<Object[]> resultados = Arrays.asList(
                new Object[]{"X", 10},
                new Object[]{"Y", 20}
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(resultados);

        Map<String, Object> respuesta = service.calcularCurulesSenado();

        Map<String, Integer> curules = (Map<String, Integer>) respuesta.get("curules");

        // No debe haber partidos válidos
        assertThat(curules).isEmpty();

        // Cifra repartidora debe ser 0 cuando no hay cocientes
        assertThat((double) respuesta.get("cifraRepartidora")).isEqualTo(0.0);

        verify(eligeRepository, times(1)).count();
        verify(eligeRepository, times(1)).contarVotosPorPartido();
    }
}
