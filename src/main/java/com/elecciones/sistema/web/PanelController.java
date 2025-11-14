package com.elecciones.sistema.web;

import com.elecciones.sistema.model.*;
import com.elecciones.sistema.repo.*;
import com.elecciones.sistema.service.RepartoSenadoService;
import com.elecciones.sistema.service.SimulacionProgresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class PanelController {

    private final UserAccountRepository userAccountRepository;
    private final CandidatoRepository candidatoRepository;
    private final PerteneceRepository perteneceRepository;
    private final EligeRepository eligeRepository;
    private final PartidoRepository partidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RepartoSenadoService repartoService;

    // ⭐ NUEVO — NO reemplaza nada
    private final SimulacionProgresoService progresoService;

    // ===========================================================
    // PANEL PRINCIPAL (TU CÓDIGO, INTACTO)
    // ===========================================================
    @GetMapping
    public String adminPanel(Model model) {
        try {
            model.addAttribute("usuarios", userAccountRepository.findAll().stream().limit(10).toList());
            model.addAttribute("candidatos", candidatoRepository.findAll().stream().limit(10).toList());
            model.addAttribute("partidos", partidoRepository.findAll().stream().limit(10).toList());
            model.addAttribute("pertenencias",
                    perteneceRepository.findAllConPartidoYCandidato().stream().limit(20).toList());

            model.addAttribute("totalUsuarios", userAccountRepository.count());
            model.addAttribute("totalCandidatos", candidatoRepository.count());
            model.addAttribute("totalPartidos", partidoRepository.count());
            model.addAttribute("puedeVotar", true);

        } catch (Exception e) {
            model.addAttribute("mensaje", "⚠️ Error cargando datos: " + e.getMessage());
        }
        return "admin";
    }

    // ===========================================================
    // CARGA DE ORDEN DE LISTAS (TU CÓDIGO ORIGINAL)
    // ===========================================================
    @PostMapping("/cargar-listas")
    public String cargarOrdenListas(@RequestParam("file") MultipartFile file, Model model) {
        int creados = 0, actualizados = 0, ignorados = 0, lineaN = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = leerCsvLimpio(reader);
            if (lineas.isEmpty()) {
                model.addAttribute("mensaje", "❌ Archivo vacío.");
                return adminPanel(model);
            }

            String[] h = splitCsv(lineas.get(0));
            if (h.length < 3 ||
                    !eq(h[0], "Orden_Candidatos") ||
                    !eq(h[1], "Partido_id") ||
                    !eq(h[2], "Cedula_Candidato")) {

                model.addAttribute("mensaje",
                        "❌ Encabezado inválido (debe tener: Orden_Candidatos, Partido_id, Cedula_Candidato).");
                return adminPanel(model);
            }

            for (int i = 1; i < lineas.size(); i++) {
                lineaN++;
                String[] c = splitCsv(lineas.get(i));
                if (c.length < 3) { ignorados++; continue; }

                String ordenTxt = limpiarTexto(c[0]);
                String partidoTxt = limpiarTexto(c[1]);
                String cedulaTxt = limpiarTexto(c[2]);

                if (partidoTxt.isEmpty() || cedulaTxt.isEmpty() || ordenTxt.isEmpty()) {
                    ignorados++; continue;
                }

                Long partidoId, cedula;
                int orden;
                try {
                    partidoId = Long.parseLong(partidoTxt);
                    cedula = Long.parseLong(cedulaTxt);
                    orden = Integer.parseInt(ordenTxt);
                } catch (NumberFormatException e) {
                    ignorados++; continue;
                }

                var partido = partidoRepository.findById(partidoId).orElse(null);
                var candidato = candidatoRepository.findById(cedula).orElse(null);
                if (partido == null || candidato == null) {
                    ignorados++;
                    continue;
                }

                var existente = perteneceRepository.findByPartido_PartidoIdAndCandidato_Cedula(partidoId, cedula);

                if (existente.isPresent()) {
                    var rel = existente.get();
                    rel.setOrdenCandidatos(orden);
                    perteneceRepository.save(rel);
                    actualizados++;
                } else {
                    var rel = new Pertenece();
                    rel.setPartido(partido);
                    rel.setCandidato(candidato);
                    rel.setOrdenCandidatos(orden);
                    perteneceRepository.save(rel);
                    creados++;
                }
            }

            model.addAttribute("mensaje", String.format(
                    "✅ Orden de listas cargado correctamente. %d creados, %d actualizados, %d ignorados.",
                    creados, actualizados, ignorados
            ));

        } catch (Exception e) {
            model.addAttribute("mensaje",
                    "❌ Error al cargar orden de listas (línea " + lineaN + "): " + e.getMessage());
            e.printStackTrace();
        }

        return adminPanel(model);
    }

    // ===========================================================
    // 🟦 CARGAR PARTIDOS (TU IMPLEMENTACIÓN)
    // ===========================================================
    @PostMapping("/cargar-partidos")
    public String cargarPartidos(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = reader.lines().toList();
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = lineas.get(i).split("[,;\\t]", -1);
                if (c.length < 4) { ignorados++; continue; }

                String idTxt = c[0].trim();
                String nombre = c[1].trim();
                String tipo = c[2].trim();
                String circ = c[3].trim();

                if (idTxt.isEmpty() || nombre.isEmpty() || tipo.isEmpty() || circ.isEmpty()) {
                    ignorados++; continue;
                }

                Long partidoId = Long.parseLong(idTxt);

                var existente = partidoRepository.findById(partidoId);

                if (existente.isPresent()) {
                    Partido p = existente.get();
                    p.setNombre(nombre);
                    p.setTipoLista(tipo);
                    p.setCircunscripcion(circ);
                    partidoRepository.save(p);
                    actualizados++;

                } else {
                    Partido p = new Partido();
                    p.setPartidoId(partidoId);
                    p.setNombre(nombre);
                    p.setTipoLista(tipo);
                    p.setCircunscripcion(circ);
                    partidoRepository.save(p);
                    creados++;
                }
            }

            redirect.addFlashAttribute("mensaje",
                    "🏛️ Partidos cargados: " + creados + " creados, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error al cargar partidos: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // 🟧 CARGAR CANDIDATOS (TU IMPLEMENTACIÓN)
    // ===========================================================
    @PostMapping("/cargar-candidatos")
    public String cargarCandidatos(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = reader.lines().toList();
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = lineas.get(i).split("[,;\\t]", -1);
                if (c.length < 3) { ignorados++; continue; }

                String cedTxt = c[0].trim();
                String nombre = c[1].trim();
                String circ = c[2].trim();

                Long cedula;
                try { cedula = Long.parseLong(cedTxt); }
                catch (Exception ex) { ignorados++; continue; }

                var existente = candidatoRepository.findById(cedula);

                if (existente.isPresent()) {
                    Candidato cand = existente.get();
                    cand.setNombre(nombre);
                    cand.setCircunscripcion(circ);
                    candidatoRepository.save(cand);
                    actualizados++;

                } else {
                    Candidato cand = new Candidato();
                    cand.setCedula(cedula);
                    cand.setNombre(nombre);
                    cand.setCircunscripcion(circ);
                    candidatoRepository.save(cand);
                    creados++;
                }
            }

            redirect.addFlashAttribute("mensaje",
                    "🧑‍💼 Candidatos cargados: " + creados + " creados, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error al cargar candidatos: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // 🟩 CARGAR VOTANTES (TU IMPLEMENTACIÓN)
    // ===========================================================
    @PostMapping("/cargar-votantes")
    public String cargarVotantes(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = reader.lines().toList();
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = lineas.get(i).split("[,;\\t]", -1);
                if (c.length < 4) { ignorados++; continue; }

                String username = c[0].trim();
                String nombre = c[1].trim();
                String correo = c[2].trim();
                String circ = c[3].trim();

                if (username.isEmpty()) { ignorados++; continue; }

                var existente = userAccountRepository.findByUsername(username);

                if (existente != null) {
                    existente.setNombreUsuario(nombre);
                    existente.setCorreoElectronico(correo);
                    existente.setCircunscripcion(circ);
                    userAccountRepository.save(existente);
                    actualizados++;

                } else {
                    UserAccount u = new UserAccount();
                    u.setUsername(username);
                    u.setNombreUsuario(nombre);
                    u.setCorreoElectronico(correo);
                    u.setCircunscripcion(circ);
                    u.setPassword(passwordEncoder.encode("123"));
                    u.setRole("VOTANTE");
                    u.setHaVotado(false);
                    userAccountRepository.save(u);
                    creados++;
                }
            }

            redirect.addFlashAttribute("mensaje",
                    "🗳️ Votantes cargados: " + creados + " creados, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error al cargar votantes: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // 🧠 SIMULACIÓN ORIGINAL (INVIOLADA)
    // ===========================================================
    @GetMapping("/simular-votacion")
    public String simularVotacion(RedirectAttributes redirectAttributes) {

        try {
            List<UserAccount> votantes = userAccountRepository.findAll()
                    .stream()
                    .filter(u -> "VOTANTE".equalsIgnoreCase(u.getRole()) && !u.isHaVotado())
                    .toList();

            List<Candidato> candidatos = candidatoRepository.findAll();
            Random random = new Random();

            for (UserAccount votante : votantes) {

                List<Candidato> candidatosCirc = candidatos.stream()
                        .filter(c -> c.getCircunscripcion().equalsIgnoreCase(votante.getCircunscripcion()))
                        .toList();

                if (candidatosCirc.isEmpty())
                    continue;

                List<Partido> partidosCirc = partidoRepository.findAll().stream()
                        .filter(p -> p.getCircunscripcion().equalsIgnoreCase(votante.getCircunscripcion()))
                        .toList();

                Partido partidoElegido = partidosCirc.get(random.nextInt(partidosCirc.size()));

                List<Pertenece> lista = perteneceRepository
                        .findByPartido_PartidoIdOrderByOrdenCandidatosAsc(partidoElegido.getPartidoId());

                Candidato elegido;

                if (lista.isEmpty()) {
                    elegido = candidatosCirc.get(random.nextInt(candidatosCirc.size()));
                } else {

                    if ("Cerrada".equalsIgnoreCase(partidoElegido.getTipoLista())) {
                        elegido = lista.get(0).getCandidato();
                    } else {
                        elegido = lista.get(random.nextInt(lista.size())).getCandidato();
                    }
                }

                Elige voto = new Elige();
                voto.setHashVotante(UUID.randomUUID());
                voto.setCandidato(elegido);
                eligeRepository.save(voto);

                votante.setHaVotado(true);
                userAccountRepository.save(votante);
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    "🎯 Simulación completada: " + votantes.size() + " votos generados correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "❌ Error durante la simulación: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // ⭐⭐ NUEVO: SIMULACIÓN LIVE SIN ROMPER TU LÓGICA ⭐⭐
    // ===========================================================
    @GetMapping("/simular-votacion-live")
    @ResponseBody
    public Map<String, Object> simularVotacionLive() {

        List<UserAccount> votantes = userAccountRepository.findAll()
                .stream()
                .filter(u -> "VOTANTE".equalsIgnoreCase(u.getRole()) && !u.isHaVotado())
                .toList();

        List<Candidato> candidatos = candidatoRepository.findAll();
        Random random = new Random();

        progresoService.reset(votantes.size());

        for (UserAccount votante : votantes) {

            List<Candidato> candidatosCirc = candidatos.stream()
                    .filter(c -> c.getCircunscripcion().equalsIgnoreCase(votante.getCircunscripcion()))
                    .toList();

            if (candidatosCirc.isEmpty()) {
                progresoService.aumentar();
                continue;
            }

            List<Partido> partidosCirc = partidoRepository.findAll().stream()
                    .filter(p -> p.getCircunscripcion().equalsIgnoreCase(votante.getCircunscripcion()))
                    .toList();

            Partido partidoElegido = partidosCirc.get(random.nextInt(partidosCirc.size()));

            List<Pertenece> lista = perteneceRepository
                    .findByPartido_PartidoIdOrderByOrdenCandidatosAsc(partidoElegido.getPartidoId());

            Candidato elegido;

            if (lista.isEmpty()) {
                elegido = candidatosCirc.get(random.nextInt(candidatosCirc.size()));
            } else {
                if ("Cerrada".equalsIgnoreCase(partidoElegido.getTipoLista())) {
                    elegido = lista.get(0).getCandidato();
                } else {
                    elegido = lista.get(random.nextInt(lista.size())).getCandidato();
                }
            }

            Elige voto = new Elige();
            voto.setHashVotante(UUID.randomUUID());
            voto.setCandidato(elegido);
            eligeRepository.save(voto);

            votante.setHaVotado(true);
            userAccountRepository.save(votante);

            progresoService.aumentar();

            try { Thread.sleep(40); } catch (Exception ignored) {}
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "done");
        return result;
    }

    // ===========================================================
    // ⭐⭐ NUEVO: CONSULTAR PROGRESO (AJAX) ⭐⭐
    // ===========================================================
    @GetMapping("/progreso-simulacion")
    @ResponseBody
    public Map<String, Integer> progresoSimulacion() {
        Map<String, Integer> m = new HashMap<>();
        m.put("total", progresoService.getTotal());
        m.put("procesados", progresoService.getProcesados());
        return m;
    }

    // ===========================================================
    // UTILIDADES CSV (TU CÓDIGO)
    // ===========================================================
    private static List<String> leerCsvLimpio(BufferedReader reader) throws Exception {
        List<String> out = new ArrayList<>();
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) { line = removerBOM(line); first = false; }
            if (line == null || line.trim().isEmpty()) continue;
            out.add(line);
        }
        return out;
    }

    private static String removerBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }

    private static String limpiarTexto(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[^0-9A-Za-zÁÉÍÓÚáéíóúÑñ@._\\- ]", "");
    }

    private static String[] splitCsv(String line) {
        String[] raw = line.split("[,;\\t]", -1);
        for (int i = 0; i < raw.length; i++) raw[i] = limpiarTexto(raw[i]);
        return raw;
    }

    private static boolean eq(String a, String b) {
        return a != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
