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
                new Object[]{"D", 50}        // No pasa el umbral
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(resultados);

        // 🔹 Ejecutar método
        Map<String, Object> respuesta = service.calcularCurulesSenado();

        // ================
        // VERIFICACIONES
        // ================

        // Total de votos
        assertThat(respuesta.get("totalVotos")).isEqualTo(1000L);

        // Umbral = 3% de 1000 = 30
        assertThat(respuesta.get("umbral")).isEqualTo(30.0);

        // Cifra repartidora debe ser positiva
        double cifraRepartidora = (double) respuesta.get("cifraRepartidora");
        assertThat(cifraRepartidora).isGreaterThan(0);

        // Curules por partido
        Map<String, Integer> curules = (Map<String, Integer>) respuesta.get("curules");

        // D NO DEBE aparecer (50 < 30)
        assertThat(curules.containsKey("D")).isFalse();

        // A, B, C sí deben aparecer
        assertThat(curules).containsKeys("A", "B", "C");

        // Cada partido debe tener al menos 1 curul (por su cantidad de votos)
        assertThat(curules.get("A")).isGreaterThan(0);
        assertThat(curules.get("B")).isGreaterThan(0);
        assertThat(curules.get("C")).isGreaterThan(0);

        // TOTAL curules = 100
        int totalAsignado = curules.values().stream().mapToInt(i -> i).sum();
        assertThat(totalAsignado).isEqualTo(100);

        // Verificar llamadas al repository
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

        // 🔹 Ningún partido supera el umbral
        assertThat(curules).isEmpty();

        // Cifra repartidora = último cociente disponible
        assertThat((double) respuesta.get("cifraRepartidora")).isGreaterThan(0);

        verify(eligeRepository, times(1)).count();
        verify(eligeRepository, times(1)).contarVotosPorPartido();
    }
}
