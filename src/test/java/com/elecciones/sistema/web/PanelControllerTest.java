package com.elecciones.sistema.web;

import com.elecciones.sistema.model.*;
import com.elecciones.sistema.repo.*;
import com.elecciones.sistema.service.RepartoSenadoService;
import com.elecciones.sistema.service.SimulacionProgresoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanelControllerTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private CandidatoRepository candidatoRepository;
    @Mock private PerteneceRepository perteneceRepository;
    @Mock private EligeRepository eligeRepository;
    @Mock private PartidoRepository partidoRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock private RepartoSenadoService repartoService;
    @Mock private SimulacionProgresoService progresoService;

    @InjectMocks private PanelController controller;

    private UserAccount u1;
    private Candidato c1;
    private Partido p1;

    @BeforeEach
    void setup() {
        u1 = new UserAccount();
        u1.setUsername("1001");
        u1.setNombreUsuario("Juan");
        u1.setRole("VOTANTE");
        u1.setHaVotado(false);

        c1 = new Candidato();
        c1.setCedula(2001L);
        c1.setNombre("Cand1");

        p1 = new Partido();
        p1.setPartidoId(1L);
        p1.setNombre("P1");
    }

    // -------------------------
    // adminPanel
    // -------------------------
    @Test
    void adminPanel_muestraDatosCorrectos() {
        when(userAccountRepository.findAll()).thenReturn(List.of(u1));
        when(candidatoRepository.findAll()).thenReturn(List.of(c1));
        when(partidoRepository.findAll()).thenReturn(List.of(p1));
        when(perteneceRepository.findAll()).thenReturn(List.of(new Pertenece()));
        when(userAccountRepository.count()).thenReturn(5L);
        when(candidatoRepository.count()).thenReturn(10L);
        when(partidoRepository.count()).thenReturn(2L);

        Model model = new ExtendedModelMap();
        String view = controller.adminPanel(model);

        assertThat(view).isEqualTo("admin");
        assertThat(model.getAttribute("totalUsuarios")).isEqualTo(5L);
        assertThat(model.getAttribute("totalCandidatos")).isEqualTo(10L);
        assertThat(model.getAttribute("puedeVotar")).isEqualTo(true);
        verify(userAccountRepository).findAll();
    }

    @Test
    void adminPanel_capturaExcepcionYponeMensaje() {
        when(userAccountRepository.findAll()).thenThrow(new RuntimeException("boom"));
        Model model = new ExtendedModelMap();
        String view = controller.adminPanel(model);
        assertThat(view).isEqualTo("admin");
        assertThat(model.getAttribute("mensaje")).asString().contains("Error cargando datos");
    }

    // -------------------------
    // cargarPartidos
    // -------------------------
    @Test
    void cargarPartidos_archivoVacio_produceMensaje() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "partidos.csv", "text/csv", new byte[0]);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String ret = controller.cargarPartidos(file, redirect);
        assertThat(ret).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Archivo vacío.");

    }

    @Test
    void cargarPartidos_encabezadoInvalido() throws Exception {
        String csv = "BAD;HEADER\n1;X;Y;Z\n";
        MockMultipartFile file = new MockMultipartFile("file", "partidos.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String ret = controller.cargarPartidos(file, redirect);
        assertThat(ret).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Encabezado inválido para Partidos.");
    }

    @Test
    void cargarPartidos_creaYactualizaPartidos_ignoraFilasIncompletas() throws Exception {
        // header + one valid + one incomplete + one update
        String csv =
                "Partido_id;Nombre_Partido;Tipo_Lista;Circunscripcion_Partido\n" +
                "10;Nuevo;Cerrada;Nacional\n" +
                "11; ; ; \n" + // incomplete -> ignored
                "12;Existente;Abierta;Local\n";

        MockMultipartFile file = new MockMultipartFile("file", "partidos.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(partidoRepository.findById(10L)).thenReturn(Optional.empty());
        Partido exist = new Partido();
        exist.setPartidoId(12L);
        exist.setNombre("Old");
        when(partidoRepository.findById(12L)).thenReturn(Optional.of(exist));

        String ret = controller.cargarPartidos(file, redirect);
        assertThat(ret).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje")).asString().contains("Partidos cargados");
        verify(partidoRepository).save(argThat(p -> p.getPartidoId()!=null && (p.getPartidoId()==10L || p.getPartidoId()==12L)));
    }

    // -------------------------
    // cargarCandidatos
    // -------------------------
    @Test
    void cargarCandidatos_archivoVacio_mensaje() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cand.csv", "text/csv", new byte[0]);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String r = controller.cargarCandidatos(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Archivo vacío.");
    }

    @Test
    void cargarCandidatos_encabezadoInvalido() throws Exception {
        String csv = "Bad;Header\n100;A;X\n";
        MockMultipartFile file = new MockMultipartFile("file", "cand.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String r = controller.cargarCandidatos(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Encabezado inválido para Candidatos.");
        
    }

    @Test
    void cargarCandidatos_creaYactualiza() throws Exception {
        String csv =
                "Cedula_Candidato;Nombre_Candidato;Circunscripcion_Candidato\n" +
                "2001;Nuevo;Nacional\n" +
                "2002;Existente;Local\n" +
                "2003;;X\n"; // ignored due to empty name

        MockMultipartFile file = new MockMultipartFile("file", "cand.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(candidatoRepository.findById(2001L)).thenReturn(Optional.empty());
        Candidato exist = new Candidato();
        exist.setCedula(2002L);
        exist.setNombre("Old");
        when(candidatoRepository.findById(2002L)).thenReturn(Optional.of(exist));

        String r = controller.cargarCandidatos(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje")).asString().contains("Candidatos cargados");
        verify(candidatoRepository, atLeastOnce()).save(any(Candidato.class));
    }

    // -------------------------
    // cargarVotantes
    // -------------------------
    @Test
    void cargarVotantes_archivoVacio_mensaje() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vot.csv", "text/csv", new byte[0]);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String r = controller.cargarVotantes(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Archivo vacío.");
    }

    @Test
    void cargarVotantes_encabezadoInvalido() throws Exception {
        String csv = "Bad;Header\n1;A;a@x\n";
        MockMultipartFile file = new MockMultipartFile("file", "vot.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String r = controller.cargarVotantes(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje"))
        .isEqualTo("❌ Encabezado inválido para Votantes.");

    }

    @Test
    void cargarVotantes_creaYactualiza() throws Exception {
        String csv =
                "Cedula_Votante;Nombre_Votante;Correo_Votante;Circunscripcion_Votante\n" +
                "5001;Gen;gen@example.com;Local\n" +
                "5002;Exist;exist@example.com;Local\n" +
                "5003;;a@b;X\n"; // ignored due to empty ced

        MockMultipartFile file = new MockMultipartFile("file", "vot.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(userAccountRepository.findByUsername("5001")).thenReturn(null);
        UserAccount exist = new UserAccount();
        exist.setUsername("5002");
        when(userAccountRepository.findByUsername("5002")).thenReturn(exist);
        when(passwordEncoder.encode("5001")).thenReturn("pw");

        String r = controller.cargarVotantes(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje")).asString().contains("Votantes cargados");
        verify(userAccountRepository, atLeastOnce()).save(any(UserAccount.class));
    }

    // -------------------------
    // cargarListas
    // -------------------------
    @Test
    void cargarListas_archivoVacio_modelMensaje() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "listas.csv", "text/csv", new byte[0]);
        Model model = new ExtendedModelMap();
        String r = controller.cargarOrdenListas(file, model);
        assertThat(r).isEqualTo("admin");
        assertThat(model.getAttribute("mensaje")).isEqualTo("❌ Archivo vacío.");
    }

    @Test
    void cargarListas_encabezadoInvalido_modelMensaje() throws Exception {
        String csv = "Bad;Header\n1;1;2001\n";
        MockMultipartFile file = new MockMultipartFile("file", "listas.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        Model model = new ExtendedModelMap();
        String r = controller.cargarOrdenListas(file, model);
        assertThat(r).isEqualTo("admin");
        assertThat(model.getAttribute("mensaje")).asString().contains("Encabezado inválido");
    }

    @Test
    void cargarListas_creaYactualiza() throws Exception {
        String csv =
                "Orden_Candidatos;Partido_id;Cedula_Candidato\n" +
                "1;1;2001\n" +
                "2;1;2002\n";

        MockMultipartFile file = new MockMultipartFile("file", "listas.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        Model model = new ExtendedModelMap();

        // partidos y candidatos exist
        Partido part = new Partido(); part.setPartidoId(1L);
        Candidato cand1 = new Candidato(); cand1.setCedula(2001L);
        Candidato cand2 = new Candidato(); cand2.setCedula(2002L);

        when(partidoRepository.findById(1L)).thenReturn(Optional.of(part));
        when(candidatoRepository.findById(2001L)).thenReturn(Optional.of(cand1));
        when(candidatoRepository.findById(2002L)).thenReturn(Optional.of(cand2));

        when(perteneceRepository.findByPartido_PartidoIdAndCandidato_Cedula(1L, 2001L)).thenReturn(Optional.empty());
        when(perteneceRepository.findByPartido_PartidoIdAndCandidato_Cedula(1L, 2002L)).thenReturn(Optional.empty());

        String r = controller.cargarOrdenListas(file, model);
        assertThat(r).isEqualTo("admin");
        assertThat(model.getAttribute("mensaje")).asString().contains("Orden cargado");
        verify(perteneceRepository, atLeastOnce()).save(any(Pertenece.class));
    }

    // -------------------------
    // CSV utils via BOM in header
    // -------------------------
    @Test
    void cargarPartidos_conBOM_encabezadoValido() throws Exception {
        // BOM char at start
        String bom = "\uFEFF";
        String csv = bom + "Partido_id;Nombre_Partido;Tipo_Lista;Circunscripcion_Partido\n1;X;T;C\n";
        MockMultipartFile file = new MockMultipartFile("file", "bom.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(partidoRepository.findById(1L)).thenReturn(Optional.empty());
        String r = controller.cargarPartidos(file, redirect);
        assertThat(r).isEqualTo("redirect:/admin");
        assertThat(redirect.getFlashAttributes().get("mensaje")).asString().contains("Partidos cargados");
    }

    // -------------------------
    // simularVotacionLive
    // -------------------------
    @Test
    void simularVotacionLive_noVotantesPendientes() {
        when(userAccountRepository.findAll()).thenReturn(List.of()); // no votantes
        when(candidatoRepository.findAll()).thenReturn(List.of(new Candidato()));

        Map<String, Object> res = controller.simularVotacionLive(10);
        assertThat(res.get("status")).isEqualTo("empty");
        verify(progresoService).reset(0);
    }

    @Test
    void simularVotacionLive_noCandidatos() {
        UserAccount pending = new UserAccount();
        pending.setRole("VOTANTE");
        pending.setHaVotado(false);
        when(userAccountRepository.findAll()).thenReturn(List.of(pending));
        when(candidatoRepository.findAll()).thenReturn(List.of()); // no candidates

        Map<String, Object> res = controller.simularVotacionLive(10);
        assertThat(res.get("status")).isEqualTo("error");
        verify(progresoService).reset(0);
    }

    @Test
    void simularVotacionLive_runningAndResetCalled() throws Exception {
        UserAccount pending = new UserAccount();
        pending.setUsername("u100");
        pending.setRole("VOTANTE");
        pending.setHaVotado(false);

        Candidato cand = new Candidato();
        cand.setCedula(9001L);

        when(userAccountRepository.findAll()).thenReturn(List.of(pending));
        when(candidatoRepository.findAll()).thenReturn(List.of(cand));

        // progresoService: verify reset and aumentar are called (aumentar may be called from background thread)
        doNothing().when(progresoService).reset(anyInt());
        doNothing().when(progresoService).aumentar();

        Map<String, Object> res = controller.simularVotacionLive(50);
        assertThat(res.get("status")).isEqualTo("running");
        assertThat(res.get("simulados")).isNotNull();
        // verify reset called with number >=1
        verify(progresoService).reset(anyInt());
    }

    // -------------------------
    // progresoSimulacion
    // -------------------------
    @Test
    void progresoSimulacion_retornaValoresDeServicio() {
        when(progresoService.getTotal()).thenReturn(5);
        when(progresoService.getProcesados()).thenReturn(2);
        Map<String, Object> res = controller.progresoSimulacion();
        assertThat(res.get("total")).isEqualTo(5);
        assertThat(res.get("procesados")).isEqualTo(2);
    }

    // -------------------------
    // restaurarSistema
    // -------------------------
    @Test
    void restaurarSistema_ok() {
        // setup: one voter has voted
        UserAccount vot = new UserAccount();
        vot.setUsername("v1");
        vot.setHaVotado(true);

        doNothing().when(eligeRepository).deleteAll();
        when(userAccountRepository.findAll()).thenReturn(List.of(vot));
        doNothing().when(userAccountRepository).saveAll(anyList());
        doNothing().when(progresoService).reset(0);

        Map<String, Object> res = controller.restaurarSistema();
        assertThat(res.get("status")).isEqualTo("ok");
        verify(eligeRepository).deleteAll();
        verify(userAccountRepository).saveAll(anyList());
        verify(progresoService).reset(0);
    }

    @Test
    void restaurarSistema_exception() {
        doThrow(new RuntimeException("boom")).when(eligeRepository).deleteAll();
        Map<String, Object> res = controller.restaurarSistema();
        assertThat(res.get("status")).isEqualTo("error");
        assertThat(((String) res.get("message"))).contains("boom");
    }
}
