package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class RepartoSenadoController {

    private final EligeRepository eligeRepository;

    @GetMapping("/admin/reparto-senado")
    public String repartoSenado(Model model) {

        // 1️⃣ Tomar votos por partido (circunscripción ordinaria)
        List<Object[]> votosPartido = eligeRepository.contarVotosPorPartido();

        // votos indígenas por candidato (NO por partido)
        List<Object[]> votosIndigenas = eligeRepository.contarVotosIndigenasPorCandidato();

        if ((votosPartido == null || votosPartido.isEmpty()) &&
                (votosIndigenas == null || votosIndigenas.isEmpty())) {
            model.addAttribute("mensaje", "Aún no existen votos registrados.");
            return "reparto-senado";
        }

        // =============================
        // 2️⃣ PROCESO DE 100 CURULES ORDINARIAS
        // =============================
        Map<String, Integer> votosMap = new LinkedHashMap<>();
        int totalVotos = 0;

        for (Object[] fila : votosPartido) {
            String partido = (String) fila[0];
            int votos = ((Number) fila[1]).intValue();

            votosMap.put(partido, votos);
            totalVotos += votos;
        }

        double umbral = totalVotos * 0.03;

        // Filtrar por umbral
        Map<String, Integer> validos = new LinkedHashMap<>();
        votosMap.forEach((p, v) -> {
            if (v >= umbral) validos.put(p, v);
        });

        int curulesOrdinarias = 100;

        List<Map<String, Object>> tablaDivisiones = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : validos.entrySet()) {
            String partido = entry.getKey();
            int votos = entry.getValue();

            List<Double> divisiones = new ArrayList<>();
            for (int i = 1; i <= curulesOrdinarias; i++) {
                divisiones.add(votos / (double) i);
            }

            Map<String, Object> fila = new HashMap<>();
            fila.put("partido", partido);
            fila.put("valores", divisiones);
            tablaDivisiones.add(fila);
        }

        List<Double> todosCocientes = new ArrayList<>();
        for (Map<String, Object> fila : tablaDivisiones) {
            todosCocientes.addAll((List<Double>) fila.get("valores"));
        }

        todosCocientes.sort(Comparator.reverseOrder());

        double cifraRepartidora = todosCocientes.get(curulesOrdinarias - 1);

        Map<String, Integer> curules = new LinkedHashMap<>();
        validos.forEach((p, v) -> curules.put(p, (int) Math.floor(v / cifraRepartidora)));

        // =============================
        // 3️⃣ PROCESO DE 2 CURULES INDÍGENAS (por candidato)
        // =============================
        List<Map<String, Object>> topIndigenas = new ArrayList<>();

        if (votosIndigenas != null && !votosIndigenas.isEmpty()) {

            List<Map<String, Object>> lista = new ArrayList<>();

            for (Object[] row : votosIndigenas) {
                Map<String, Object> m = new HashMap<>();
                m.put("candidato", row[0]);
                m.put("votos", ((Number) row[1]).intValue());
                lista.add(m);
            }

            lista.sort((a, b) ->
                    Integer.compare((int) b.get("votos"), (int) a.get("votos")));

            // Tomar los 2 más votados (regla constitucional)
            topIndigenas = lista.subList(0, Math.min(2, lista.size()));
        }

        // =============================
        // 4️⃣ Enviar al HTML
        // =============================
        model.addAttribute("totalVotos", totalVotos);
        model.addAttribute("umbral", (int) umbral);
        model.addAttribute("cifraRepartidora", cifraRepartidora);

        model.addAttribute("curulesOrdinarias", curules);
        model.addAttribute("tablaDivisiones", tablaDivisiones);
        model.addAttribute("partidosUmbral", validos.entrySet());

        model.addAttribute("curulesIndigenas", topIndigenas);

        return "reparto-senado";
    }
}
