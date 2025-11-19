package com.elecciones.sistema.web;

import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
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

        // 3️⃣ Total de votos emitidos
        long totalVotosEmitidos = eligeRepository.count();

        // 🔥 Si no hay votos → mostrar mensaje y tabla vacía
        if (totalVotosEmitidos == 0) {
            model.addAttribute("mensaje", "Aún no existen votos registrados.");
            model.addAttribute("totalVotantes", totalVotantes);
            model.addAttribute("votantesEfectivos", 0);
            model.addAttribute("totalVotosEmitidos", 0);
            model.addAttribute("sumaVotosCandidatos", 0);
            model.addAttribute("porcentajeParticipacion", 0);
            model.addAttribute("verificacionOk", true);
            model.addAttribute("resultados", Collections.emptyList());
            return "resultados";
        }

        // 4️⃣ Resultados por candidato
        List<Object[]> resultados = eligeRepository.contarVotosPorCandidato();

        // 5️⃣ Suma total de votos por candidato (para consistencia)
        long sumaVotosCandidatos = resultados.stream()
                .mapToLong(r -> ((Number) r[2]).longValue())
                .sum();

        // 6️⃣ Porcentaje de participación
        double porcentajeParticipacion = (totalVotantes > 0)
                ? (votantesEfectivos * 100.0 / totalVotantes)
                : 0.0;

        // 7️⃣ Verificación de integridad electoral
        boolean verificacionOk = (votantesEfectivos == sumaVotosCandidatos);

        // 📊 Agregar al modelo
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
