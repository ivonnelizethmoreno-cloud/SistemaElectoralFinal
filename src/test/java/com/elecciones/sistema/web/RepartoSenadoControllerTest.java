package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepartoSenadoControllerTest {

    @Mock
    private EligeRepository eligeRepository;

    @InjectMocks
    private RepartoSenadoController controller;

    @Test
    void testRepartoSenadoCompleto() {

        // =============================
        // 1️⃣ Caso sin votos
        // =============================
        Model model1 = new ExtendedModelMap();

        when(eligeRepository.contarVotosPorPartido()).thenReturn(Collections.emptyList());
        when(eligeRepository.contarVotosIndigenasPorCandidato()).thenReturn(Collections.emptyList());

        String r1 = controller.repartoSenado(model1);

        assertThat(r1).isEqualTo("reparto-senado");
        assertThat(model1.getAttribute("mensaje")).isEqualTo("Aún no existen votos registrados.");


        // =============================
        // 2️⃣ Caso con votos ordinarios e indígenas
        // =============================

        // votos ordinarios: PartidoA = 1000, PartidoB = 400, PartidoC = 50
        List<Object[]> votosOrdinarios = Arrays.asList(
                new Object[]{"PartidoA", 1000},
                new Object[]{"PartidoB", 400},
                new Object[]{"PartidoC", 50}
        );

        // votos indígenas por candidato
        List<Object[]> votosIndigenas = Arrays.asList(
                new Object[]{"Indigena1", 300},
                new Object[]{"Indigena2", 200},
                new Object[]{"Indigena3", 100}
        );

        when(eligeRepository.contarVotosPorPartido()).thenReturn(votosOrdinarios);
        when(eligeRepository.contarVotosIndigenasPorCandidato()).thenReturn(votosIndigenas);

        Model model2 = new ExtendedModelMap();
        String r2 = controller.repartoSenado(model2);

        assertThat(r2).isEqualTo("reparto-senado");


        // =============================
        // Verificación del cálculo básico
        // =============================

        int totalVotos = (int) model2.getAttribute("totalVotos");
        assertThat(totalVotos).isEqualTo(1450);

        int umbral = (int) model2.getAttribute("umbral");
        assertThat(umbral).isEqualTo((int) (1450 * 0.03)); // 43

        // PartidoC = 50, supera el umbral (43), así que entra
        // Ningún partido debería ser filtrado

        @SuppressWarnings("unchecked")
        Iterable<Map.Entry<String, Integer>> partidosUmbral =
                (Iterable<Map.Entry<String, Integer>>) model2.getAttribute("partidosUmbral");

        Map<String, Integer> mapaUmbral = new LinkedHashMap<>();
        partidosUmbral.forEach(e -> mapaUmbral.put(e.getKey(), e.getValue()));

        assertThat(mapaUmbral)
                .containsEntry("PartidoA", 1000)
                .containsEntry("PartidoB", 400)
                .containsEntry("PartidoC", 50);


        // =============================
        // Validación de cifra repartidora
        // =============================
        double cifra = (double) model2.getAttribute("cifraRepartidora");
        assertThat(cifra).isGreaterThan(0);


        // =============================
        // Validación de curules ordinarias
        // =============================
        @SuppressWarnings("unchecked")
        Map<String, Integer> curules =
                (Map<String, Integer>) model2.getAttribute("curulesOrdinarias");

        assertThat(curules).isNotEmpty();
        assertThat(curules.keySet()).contains("PartidoA", "PartidoB", "PartidoC");


        // =============================
        // Validación de curules indígenas (top 2)
        // =============================
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> curulesIndigenas =
                (List<Map<String, Object>>) model2.getAttribute("curulesIndigenas");

        assertThat(curulesIndigenas).hasSize(2);

        assertThat(curulesIndigenas.get(0).get("candidato")).isEqualTo("Indigena1");
        assertThat(curulesIndigenas.get(1).get("candidato")).isEqualTo("Indigena2");
    }
}
