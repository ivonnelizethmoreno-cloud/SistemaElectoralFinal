package com.elecciones.sistema.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ResultadosController {

    private final EligeRepository eligeRepository;
    private final UserAccountRepository userAccountRepository;

    @GetMapping("/admin/resultados")
    public String mostrarResultados(Model model) {

        // 1️⃣ Total votantes registrados
        long totalVotantes = userAccountRepository.countByRoleIgnoreCase("VOTANTE");

        // 2️⃣ Votantes efectivos (que votaron)
        long votantesEfectivos = userAccountRepository.countByRoleIgnoreCaseAndHaVotadoTrue("VOTANTE");

        // 3️⃣ Total de votos emitidos (registros en Elige)
        long totalVotosEmitidos = eligeRepository.count();

        // 4️⃣ Resultados por candidato (cédula, nombre, conteo)
        List<Object[]> resultados = eligeRepository.contarVotosPorCandidato();

        // 5️⃣ Suma total de votos asignados a candidatos
        long sumaVotosCandidatos = resultados.stream()
                .mapToLong(r -> ((Number) r[2]).longValue())
                .sum();

        // 6️⃣ Porcentaje de participación
        double porcentajeParticipacion = (totalVotantes > 0)
                ? (votantesEfectivos * 100.0 / totalVotantes)
                : 0.0;

        // 7️⃣ Verificación de integridad electoral
        boolean verificacionOk = (votantesEfectivos == sumaVotosCandidatos);

        // 📊 Enviar datos al modelo
        model.addAttribute("totalVotantes", totalVotantes);
        model.addAttribute("votantesEfectivos", votantesEfectivos);
        model.addAttribute("totalVotosEmitidos", totalVotosEmitidos);
        model.addAttribute("sumaVotosCandidatos", sumaVotosCandidatos);
        model.addAttribute("porcentajeParticipacion", porcentajeParticipacion);
        model.addAttribute("verificacionOk", verificacionOk);
        model.addAttribute("resultados", resultados);

        return "resultados";
    }
}
