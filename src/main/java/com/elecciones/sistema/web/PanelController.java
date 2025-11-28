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
    private final SimulacionProgresoService progresoService;
    
    private static final Random RANDOM = new Random();

    // ===========================================================
    // PANEL PRINCIPAL
    // ===========================================================
    @GetMapping
    public String adminPanel(Model model) {
        try {
            model.addAttribute("usuarios", userAccountRepository.findAll().stream().limit(10).toList());
            model.addAttribute("candidatos", candidatoRepository.findAll().stream().limit(10).toList());
            model.addAttribute("partidos", partidoRepository.findAll().stream().limit(10).toList());
            model.addAttribute("pertenencias", perteneceRepository.findAll().stream().limit(20).toList());

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
    // 🟨 CARGA DE PARTIDOS
    // ===========================================================
    @PostMapping("/cargar-partidos")
    public String cargarPartidos(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = leerCsv(reader);
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            String[] h = splitCsv(lineas.get(0));

            if (!encabezado(h, "Partido_id", "Nombre_Partido", "Tipo_Lista", "Circunscripcion_Partido")) {
                redirect.addFlashAttribute("mensaje", "❌ Encabezado inválido para Partidos.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = splitCsv(lineas.get(i));
                if (c.length < 4) {
                    ignorados++;
                    continue;
                }

                String id = c[0].trim();
                String nombre = c[1].trim();
                String tipo = c[2].trim();
                String circ = c[3].trim();

                if (id.isEmpty() || nombre.isEmpty()) {
                    ignorados++;
                    continue;
                }

                Long partidoId = Long.parseLong(id);
                Optional<Partido> existente = partidoRepository.findById(partidoId);

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
                    "🏛️ Partidos cargados: " + creados + " nuevos, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error cargando PARTIDOS: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // 🟦 CARGA DE CANDIDATOS
    // ===========================================================
    @PostMapping("/cargar-candidatos")
    public String cargarCandidatos(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = leerCsv(reader);
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            String[] h = splitCsv(lineas.get(0));

            if (!encabezado(h, "Cedula_Candidato", "Nombre_Candidato", "Circunscripcion_Candidato")) {
                redirect.addFlashAttribute("mensaje", "❌ Encabezado inválido para Candidatos.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {

                String[] c = splitCsv(lineas.get(i));
                if (c.length < 3) {
                    ignorados++;
                    continue;
                }

                String ced = c[0].trim();
                String nombre = c[1].trim();
                String circ = c[2].trim();

                if (ced.isEmpty() || nombre.isEmpty()) {
                    ignorados++;
                    continue;
                }

                Long cedula = Long.parseLong(ced);
                Optional<Candidato> existente = candidatoRepository.findById(cedula);

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
                    "🧑‍💼 Candidatos cargados: " + creados + " nuevos, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error cargando CANDIDATOS: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // 🟩 CARGA DE VOTANTES
    // ===========================================================
    @PostMapping("/cargar-votantes")
    public String cargarVotantes(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = leerCsv(reader);
            if (lineas.isEmpty()) {
                redirect.addFlashAttribute("mensaje", "❌ Archivo vacío.");
                return "redirect:/admin";
            }

            String[] h = splitCsv(lineas.get(0));

            if (!encabezado(h, "Cedula_Votante", "Nombre_Votante", "Correo_Votante", "Circunscripcion_Votante")) {
                redirect.addFlashAttribute("mensaje", "❌ Encabezado inválido para Votantes.");
                return "redirect:/admin";
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = splitCsv(lineas.get(i));
                if (c.length < 4) {
                    ignorados++;
                    continue;
                }

                String ced = c[0].trim();
                String nombre = c[1].trim();
                String correo = c[2].trim();
                String circ = c[3].trim();

                if (ced.isEmpty()) {
                    ignorados++;
                    continue;
                }

                UserAccount existente = userAccountRepository.findByUsername(ced);

                if (existente != null) {
                    existente.setNombreUsuario(nombre);
                    existente.setCorreoElectronico(correo);
                    existente.setCircunscripcion(circ);
                    userAccountRepository.save(existente);
                    actualizados++;

                } else {
                    UserAccount u = new UserAccount();
                    u.setUsername(ced);
                    u.setNombreUsuario(nombre);
                    u.setCorreoElectronico(correo);
                    u.setCircunscripcion(circ);
                    u.setPassword(passwordEncoder.encode(ced));
                    u.setRole("VOTANTE");
                    u.setHaVotado(false);
                    userAccountRepository.save(u);
                    creados++;
                }
            }

            redirect.addFlashAttribute("mensaje",
                    "🗳️ Votantes cargados: " + creados + " nuevos, " + actualizados + " actualizados.");

        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje",
                    "❌ Error cargando VOTANTES: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // ===========================================================
    // CARGA DE LISTAS (PERTENECE)
    // ===========================================================
    @PostMapping("/cargar-listas")
    public String cargarOrdenListas(@RequestParam("file") MultipartFile file, Model model) {

        int creados = 0, actualizados = 0, ignorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lineas = leerCsv(reader);
            if (lineas.isEmpty()) {
                model.addAttribute("mensaje", "❌ Archivo vacío.");
                return adminPanel(model);
            }

            String[] h = splitCsv(lineas.get(0));

            if (!encabezado(h, "Orden_Candidatos", "Partido_id", "Cedula_Candidato")) {
                model.addAttribute("mensaje",
                        "❌ Encabezado inválido (Orden_Candidatos;Partido_id;Cedula_Candidato)");
                return adminPanel(model);
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] c = splitCsv(lineas.get(i));
                if (c.length < 3) {
                    ignorados++;
                    continue;
                }

                String ordenTxt = c[0].trim();
                String partidoTxt = c[1].trim();
                String cedulaTxt = c[2].trim();

                if (ordenTxt.isEmpty() || partidoTxt.isEmpty() || cedulaTxt.isEmpty()) {
                    ignorados++;
                    continue;
                }

                Long partidoId = Long.parseLong(partidoTxt);
                Long cedula = Long.parseLong(cedulaTxt);
                int orden = Integer.parseInt(ordenTxt);

                Optional<Partido> partido = partidoRepository.findById(partidoId);
                Optional<Candidato> candidato = candidatoRepository.findById(cedula);

                if (partido.isEmpty() || candidato.isEmpty()) {
                    ignorados++;
                    continue;
                }

                Optional<Pertenece> existente =
                        perteneceRepository.findByPartido_PartidoIdAndCandidato_Cedula(partidoId, cedula);

                if (existente.isPresent()) {
                    Pertenece p = existente.get();
                    p.setOrdenCandidatos(orden);
                    perteneceRepository.save(p);
                    actualizados++;

                } else {
                    Pertenece p = new Pertenece();
                    p.setPartido(partido.get());
                    p.setCandidato(candidato.get());
                    p.setOrdenCandidatos(orden);
                    perteneceRepository.save(p);
                    creados++;
                }
            }

            model.addAttribute("mensaje",
                    "📋 Orden cargado: " + creados + " nuevos, " + actualizados + " actualizados.");

        } catch (Exception e) {
            model.addAttribute("mensaje",
                    "❌ Error cargando LISTAS: " + e.getMessage());
        }

        return adminPanel(model);
    }

    // ===========================================================
    // UTILIDADES DE CSV (CORREGIDAS)
    // ===========================================================

    private static List<String> leerCsv(BufferedReader reader) throws Exception {
        List<String> out = new ArrayList<>();
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                line = removerBOM(line);
                first = false;
            }
            if (!line.trim().isEmpty())
                out.add(line);
        }
        return out;
    }

    private static String removerBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF')
            return s.substring(1);
        return s;
    }

    // ❗ CSV REAL: SE USA SÓLO `;` COMO SEPARADOR
    private static String[] splitCsv(String line) {
        return line.split(";", -1);
    }

    // ✔ Encabezado flexible: compara sin eliminar caracteres válidos
    private static boolean encabezado(String[] arr, String... esperado) {
        if (arr.length < esperado.length) return false;
        for (int i = 0; i < esperado.length; i++) {
            if (!arr[i].trim().equalsIgnoreCase(esperado[i])) return false;
        }
        return true;
    }

    // ===========================================================
    // SIMULACIÓN LIVE – CON PORCENTAJE
    // ===========================================================
    @GetMapping("/simular-votacion-live")
    @ResponseBody
    public Map<String, Object> simularVotacionLive(
            @RequestParam(name = "p", defaultValue = "10") int porcentaje) {

        // Normalizar porcentaje (entre 1 y 100)
        if (porcentaje < 1) porcentaje = 1;
        if (porcentaje > 100) porcentaje = 100;

        // Votantes pendientes de votar
        List<UserAccount> votantesPendientes = userAccountRepository.findAll()
            .stream()
            .filter(u -> "VOTANTE".equalsIgnoreCase(u.getRole()) && !u.isHaVotado())
            .toList();

        List<Candidato> candidatos = candidatoRepository.findAll();
        Random random = Random();

        if (votantesPendientes.isEmpty()) {
            progresoService.reset(0);
            return Map.of("status", "empty", "message", "No hay votantes pendientes.");
        }

        if (candidatos.isEmpty()) {
            progresoService.reset(0);
            return Map.of("status", "error", "message", "No hay candidatos cargados.");
        }

        // Cuántos voy a simular según el porcentaje
        int totalPendientes = votantesPendientes.size();
        int cantidadASimular = (int) Math.round(totalPendientes * (porcentaje / 100.0));

        if (cantidadASimular <= 0) {
            cantidadASimular = 1;
        }

        // Mezclar y tomar sólo el subconjunto a simular
        List<UserAccount> listaTrabajo = new ArrayList<>(votantesPendientes);
        Collections.shuffle(listaTrabajo, random);
       /* listaTrabajo = listaTrabajo.subList(0, cantidadASimular); */
        List<UserAccount> subLista = new ArrayList<>(listaTrabajo.subList(0, cantidadASimular));

        // Inicializar barra de progreso
        progresoService.reset(cantidadASimular);

        new Thread(() -> {
            try {
                /*for (UserAccount votante : listaTrabajo) { */
                for (UserAccount votante : subLista) {

                    Candidato elegido = candidatos.get(random.nextInt(candidatos.size()));

                    Elige voto = new Elige();
                    voto.setHashVotante(UUID.randomUUID());
                    voto.setCandidato(elegido);
                    eligeRepository.save(voto);

                    votante.setHaVotado(true);
                    userAccountRepository.save(votante);

                    // Avanza la barra (en memoria)
                    progresoService.aumentar();

                    Thread.sleep(10); // pequeña pausa para que se vea el avance
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return Map.of(
                "status", "running",
                "totalPendientes", totalPendientes,
                "simulados", cantidadASimular,
                "porcentaje", porcentaje
        );
    }

    // ===========================================================
    // ENDPOINT DE PROGRESO PARA LA BARRA LIVE
    // ===========================================================
    @GetMapping("/progreso-simulacion")
    @ResponseBody
    public Map<String, Object> progresoSimulacion() {
        return Map.of(
                "total", progresoService.getTotal(),
                "procesados", progresoService.getProcesados()
        );
    }
    // ===========================================================
// RESTAURAR SISTEMA – BORRAR VOTOS Y REINICIAR ESTADO
// ===========================================================
    @PostMapping("/restaurar-sistema")
    @ResponseBody
    public Map<String, Object> restaurarSistema() {

        try {
            // 1️⃣ Borrar todos los votos
            eligeRepository.deleteAll();

            // 2️⃣ Dejar a todos los votantes como "no han votado"
            List<UserAccount> votantes = userAccountRepository.findAll();
            for (UserAccount v : votantes) {
                v.setHaVotado(false);
            }
            userAccountRepository.saveAll(votantes);

            // 3️⃣ Resetear barra de progreso
            progresoService.reset(0);

            return Map.of("status", "ok", "message", "Sistema restaurado correctamente.");

        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

}
