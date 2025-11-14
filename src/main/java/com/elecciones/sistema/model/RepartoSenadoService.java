package com.elecciones.sistema.service;

import com.elecciones.sistema.repo.EligeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepartoSenadoService {

    private final EligeRepository eligeRepository;

    public Map<String, Object> calcularCurulesSenado() {

        // 1️⃣ Obtener total de votos válidos
        long totalVotos = eligeRepository.count();

        // 2️⃣ Calcular umbral (3%)
        double umbral = totalVotos * 0.03;

        // 3️⃣ Obtener votos por partido
        List<Object[]> resultados = eligeRepository.contarVotosPorPartido();
        Map<String, Integer> votosPorPartido = new LinkedHashMap<>();
        for (Object[] fila : resultados) {
            String nombre = (String) fila[0];
            int votos = ((Number) fila[1]).intValue();
            votosPorPartido.put(nombre, votos);
        }

        // 4️⃣ Filtrar partidos que superan el umbral
        Map<String, Integer> partidosValidos = votosPorPartido.entrySet().stream()
                .filter(e -> e.getValue() >= umbral)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        // 5️⃣ Calcular cocientes (dividir por 1..100)
        int totalCurules = 100;
        List<Map.Entry<String, Double>> cocientes = new ArrayList<>();

        for (var entry : partidosValidos.entrySet()) {
            for (int i = 1; i <= totalCurules; i++) {
                cocientes.add(Map.entry(entry.getKey(), entry.getValue() / (double) i));
            }
        }

        // 6️⃣ Ordenar cocientes descendentes
        cocientes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 7️⃣ Cifra repartidora = valor del cociente número 100
        double cifraRepartidora = cocientes.size() >= totalCurules
                ? cocientes.get(totalCurules - 1).getValue()
                : cocientes.get(cocientes.size() - 1).getValue();

        // 8️⃣ Asignar curules
        Map<String, Integer> curulesPorPartido = new LinkedHashMap<>();
        for (var entry : partidosValidos.entrySet()) {
            int curules = (int) Math.floor(entry.getValue() / cifraRepartidora);
            curulesPorPartido.put(entry.getKey(), curules);
        }

        // 9️⃣ Resultado
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalVotos", totalVotos);
        resultado.put("umbral", umbral);
        resultado.put("cifraRepartidora", cifraRepartidora);
        resultado.put("curules", curulesPorPartido);

        return resultado;
    }
}
