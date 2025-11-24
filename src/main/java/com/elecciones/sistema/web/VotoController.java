package com.elecciones.sistema.web;

import com.elecciones.sistema.model.Elige;
import com.elecciones.sistema.model.Pertenece;
import com.elecciones.sistema.model.Partido;
import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.CandidatoRepository;
import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.PerteneceRepository;
import com.elecciones.sistema.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/votante")
public class VotoController {

    private final UserAccountRepository userAccountRepository;
    private final PerteneceRepository perteneceRepository;
    private final CandidatoRepository candidatoRepository;
    private final EligeRepository eligeRepository;

    // ===========================================================
    // 🔹 MUESTRA TARJETÓN
    // ===========================================================
    @GetMapping
    public String mostrarTarjeton(Authentication auth,
                                  @RequestParam(value = "tipo", required = false) String tipoTarjeton,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (auth == null) {
            redirectAttributes.addFlashAttribute("mensaje", "⚠️ Debes iniciar sesión.");
            return "redirect:/login";
        }

        UserAccount usuario = userAccountRepository.findByUsername(auth.getName());

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensaje", "⚠️ Usuario no encontrado.");
            return "redirect:/login";
        }

        // 🔥 BLOQUEO SI YA VOTÓ
        if (usuario.isHaVotado()) {
            model.addAttribute("mensaje",
                    "Señor(a) Ciudadano(a) usted ya votó, sólo se permite un intento");
            return "bloqueo-post-voto";
        }

        // Normalización circunscripción
        String circVotNorm = normalize(usuario.getCircunscripcion());

        // Selector indígena
        if (circVotNorm.equals("INDIGENA") && tipoTarjeton == null) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("esIndigena", true);
            return "votar_selector";
        }

        if (tipoTarjeton == null) tipoTarjeton = "ordinario";
        tipoTarjeton = tipoTarjeton.trim().toUpperCase(Locale.ROOT);

        final String circVotFinal = circVotNorm;
        final String tipoTarjFinal = tipoTarjeton;

        // Obtener pertenencias filtradas
        List<Pertenece> filtradas = perteneceRepository.findAllConPartidoYCandidato()
                .stream()
                .filter(p -> {
                    String cp = normalize(p.getPartido().getCircunscripcion());
                    String cc = normalize(p.getCandidato().getCircunscripcion());

                    if (circVotFinal.equals("ORDINARIA")) {
                        return cp.equals("ORDINARIA") && cc.equals("ORDINARIA");
                    }

                    if (circVotFinal.equals("INDIGENA")) {
                        if (tipoTarjFinal.equals("ORDINARIO")) {
                            return cp.equals("ORDINARIA") && cc.equals("ORDINARIA");
                        }
                        if (tipoTarjFinal.equals("INDIGENA")) {
                            return cp.equals("INDIGENA") && cc.equals("INDIGENA");
                        }
                    }

                    return false;
                })
                .sorted(Comparator.comparing(p -> p.getPartido().getPartidoId()))
                .collect(Collectors.toList());

        // Agrupar por partido
        Map<Partido, List<Pertenece>> agrupadas = new LinkedHashMap<>();
        filtradas.forEach(p ->
                agrupadas.computeIfAbsent(p.getPartido(), k -> new ArrayList<>()).add(p)
        );

        agrupadas.forEach((p, lista) ->
                lista.sort(Comparator.comparing(Pertenece::getOrdenCandidatos))
        );

        model.addAttribute("usuario", usuario);
        model.addAttribute("partidos", agrupadas);
        model.addAttribute("tipoTarjeton", tipoTarjFinal);
        model.addAttribute("circunscripcionVotante", circVotFinal);

        return "votar";
    }

    // Normalizador
    private String normalize(String s) {
        if (s == null) return "";
        s = s.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");

        if (s.startsWith("ORD")) return "ORDINARIA";
        if (s.startsWith("IND")) return "INDIGENA";

        return "";
    }

    // ===========================================================
    // 🔹 REGISTRAR VOTO (INCLUYE VOTO EN BLANCO)
    // ===========================================================
    @PostMapping("/emitir")
    public String emitirVoto(
            @RequestParam(name = "candidatoIdHidden", required = false) String candidatoIdRaw,
            @RequestParam(name = "partidoIdCerrado", required = false) Long partidoIdCerrado,
            RedirectAttributes redirect,
            Authentication auth) {

        if (auth == null) {
            redirect.addFlashAttribute("mensaje", "⚠️ Debes iniciar sesión.");
            return "redirect:/login";
        }

        UserAccount usuario = userAccountRepository.findByUsername(auth.getName());

        if (usuario == null) {
            redirect.addFlashAttribute("mensaje", "⚠️ Usuario no encontrado.");
            return "redirect:/login";
        }

        if (usuario.isHaVotado()) {
            redirect.addFlashAttribute("mensaje", "⚠️ Ya votaste.");
            return "redirect:/votante/gracias";
        }

        Long candidatoFinal = null;

        // 🟨 VOTO EN BLANCO
        if ("BLANCO".equalsIgnoreCase(candidatoIdRaw)) {

            String circ = normalize(usuario.getCircunscripcion());

            if (circ.equals("ORDINARIA")) candidatoFinal = 9000000001L;
            else if (circ.equals("INDIGENA")) candidatoFinal = 9000000002L;
            else {
                redirect.addFlashAttribute("mensaje", "❌ Error procesando voto en blanco.");
                return "redirect:/votante";
            }
        }

        // 🎯 LISTA CERRADA
        else if (partidoIdCerrado != null) {

            List<Pertenece> lista =
                    perteneceRepository.findByPartido_PartidoIdOrderByOrdenCandidatosAsc(partidoIdCerrado);

            if (lista.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Partido inválido.");
                return "redirect:/votante";
            }

            candidatoFinal = lista.get(0).getCandidato().getCedula();
        }

        // 🎯 LISTA ABIERTA
        else if (candidatoIdRaw != null) {
            try {
                candidatoFinal = Long.parseLong(candidatoIdRaw);
            } catch (Exception e) {
                redirect.addFlashAttribute("mensaje", "❌ Opción inválida.");
                return "redirect:/votante";
            }
        }

        else {
            redirect.addFlashAttribute("mensaje", "⚠️ Debes seleccionar una opción.");
            return "redirect:/votante";
        }

        // Validar candidato
        var candidato = candidatoRepository.findById(candidatoFinal).orElse(null);

        if (candidato == null) {
            redirect.addFlashAttribute("mensaje", "❌ Candidato inválido.");
            return "redirect:/votante";
        }

        // Guardar voto
        /*UUID hash = UUID.nameUUIDFromBytes(usuario.getUsername().getBytes());*/
        UUID hash = UUID.randomUUID();

        Elige voto = Elige.builder()
                /*.hashVotante(hash)*/
                .hashVotante(UUID.randomUUID())
                .candidato(candidato)
                .build();

        eligeRepository.save(voto);

        usuario.setHaVotado(true);
        userAccountRepository.save(usuario);

        redirect.addFlashAttribute("mensaje", "🗳️ Voto registrado correctamente.");
        return "redirect:/votante/gracias";
    }

    // ===========================================================
    // 🔹 PANTALLA GRACIAS
    // ===========================================================
    @GetMapping("/gracias")
    public String mostrarGracias(Authentication auth, Model model) {
        if (auth != null) {
            UserAccount usuario = userAccountRepository.findByUsername(auth.getName());
            model.addAttribute("usuario", usuario);
        }
        return "gracias";
    }
}
