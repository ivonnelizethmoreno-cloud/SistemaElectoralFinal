package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.PartidoRepository;
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

        // 1️⃣ Contar votos por partido desde BD
        List<Object[]> votosPartido = eligeRepository.contarVotosPorPartido();

        Map<String, Integer> votosMap = new LinkedHashMap<>();
        int totalVotos = 0;

        for (Object[] fila : votosPartido) {
            String partido = (String) fila[0];
            int votos = ((Number) fila[1]).intValue();
            votosMap.put(partido, votos);
            totalVotos += votos;
        }

        // Si no hay votos → no hay reparto
        if (totalVotos == 0) {
            model.addAttribute("mensaje", "No existen votos válidos para realizar el reparto.");
            return "error";
        }

        // 2️⃣ Umbral 3%
        double umbral = totalVotos * 0.03;

        // 3️⃣ Filtrar partidos que superan el umbral
        Map<String, Integer> validos = new LinkedHashMap<>();
        votosMap.forEach((p, v) -> {
            if (v >= umbral) validos.put(p, v);
        });

        if (validos.isEmpty()) {
            model.addAttribute("mensaje", "Ningún partido superó el umbral del 3%.");
            return "error";
        }

        int curulesTotales = 100;

        // 4️⃣ Construir tabla de divisiones (para mostrar en HTML)
        List<Map<String, Object>> tablaDivisiones = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : validos.entrySet()) {
            String partido = entry.getKey();
            int votos = entry.getValue();

            List<Double> divisiones = new ArrayList<>();
            for (int i = 1; i <= curulesTotales; i++) {
                divisiones.add(votos / (double) i);
            }

            Map<String, Object> fila = new HashMap<>();
            fila.put("partido", partido);
            fila.put("valores", divisiones);

            tablaDivisiones.add(fila);
        }

        // 5️⃣ Obtener todos los cocientes para la cifra repartidora
        List<Double> todosCocientes = new ArrayList<>();
        for (Map<String, Object> fila : tablaDivisiones) {
            List<Double> vals = (List<Double>) fila.get("valores");
            todosCocientes.addAll(vals);
        }

        todosCocientes.sort(Comparator.reverseOrder());

        double cifraRepartidora = todosCocientes.get(curulesTotales - 1);
        if (cifraRepartidora == 0) cifraRepartidora = 1.0;

        // 6️⃣ Calcular curules finales
        Map<String, Integer> curules = new LinkedHashMap<>();
        double cifraFinal = cifraRepartidora;

        validos.forEach((p, v) -> {
            curules.put(p, (int) Math.floor(v / cifraFinal));
        });

        // --- Enviar al HTML ---
        model.addAttribute("totalVotos", totalVotos);
        model.addAttribute("umbral", (int) umbral);
        model.addAttribute("cifraRepartidora", cifraFinal);
        model.addAttribute("curules", curules);

        model.addAttribute("tablaDivisiones", tablaDivisiones);
        model.addAttribute("partidosUmbral", validos.entrySet());

        return "reparto-senado";
    }
}
