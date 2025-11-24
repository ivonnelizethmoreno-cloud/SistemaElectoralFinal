package com.elecciones.sistema.web;

import com.elecciones.sistema.model.Candidato;
import com.elecciones.sistema.model.Elige;
import com.elecciones.sistema.model.Partido;
import com.elecciones.sistema.model.Pertenece;
import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.CandidatoRepository;
import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.repo.PerteneceRepository;
import com.elecciones.sistema.repo.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotoControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PerteneceRepository perteneceRepository;
    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private EligeRepository eligeRepository;

    @InjectMocks
    private VotoController controller;

    private UserAccount usuarioOrdinario;
    private Authentication authOrdinario;

    @BeforeEach
    void setUp() {
        usuarioOrdinario = UserAccount.builder()
                .username("carlos")
                .password("pwd")
                .circunscripcion("Ordinaria")
                .haVotado(false)
                .build();
        authOrdinario = new UsernamePasswordAuthenticationToken(usuarioOrdinario.getUsername(), usuarioOrdinario.getPassword());
    }

    @Test
    void muestraSelectorParaVotanteIndigena() {
        UserAccount usuarioIndigena = UserAccount.builder()
                .username("ana")
                .password("pwd")
                .circunscripcion("Indígena")
                .haVotado(false)
                .build();
        Authentication authIndigena = new UsernamePasswordAuthenticationToken(usuarioIndigena.getUsername(), usuarioIndigena.getPassword());

        when(userAccountRepository.findByUsername("ana")).thenReturn(usuarioIndigena);

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String vista = controller.mostrarTarjeton(authIndigena, null, model, redirectAttributes);

        assertThat(vista).isEqualTo("votar_selector");
        assertThat(model.getAttribute("esIndigena")).isEqualTo(true);
        assertThat(model.getAttribute("usuario")).isEqualTo(usuarioIndigena);
    }

    @Test
    void bloqueaVistaSiUsuarioYaVoto() {
        usuarioOrdinario.setHaVotado(true);
        when(userAccountRepository.findByUsername(usuarioOrdinario.getUsername())).thenReturn(usuarioOrdinario);

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String vista = controller.mostrarTarjeton(authOrdinario, "ordinario", model, redirectAttributes);

        assertThat(vista).isEqualTo("bloqueo-post-voto");
        assertThat(model.getAttribute("mensaje")).isEqualTo("Señor(a) Ciudadano(a) usted ya votó, sólo se permite un intento");
        verifyNoInteractions(perteneceRepository);
    }

    @Test
    void filtraTarjetonSegunCircunscripcionElegida() {
        Partido partidoOrdinario = Partido.builder().partidoId(1L).circunscripcion("ORDINARIA").build();
        Partido partidoIndigena = Partido.builder().partidoId(2L).circunscripcion("INDIGENA").build();

        Candidato candidatoOrdinario = Candidato.builder().cedula(10L).circunscripcion("ORDINARIA").build();
        Candidato candidatoIndigena = Candidato.builder().cedula(20L).circunscripcion("INDIGENA").build();

        Pertenece ordinario = Pertenece.builder()
                .partido(partidoOrdinario)
                .candidato(candidatoOrdinario)
                .ordenCandidatos(1)
                .build();

        Pertenece indigena = Pertenece.builder()
                .partido(partidoIndigena)
                .candidato(candidatoIndigena)
                .ordenCandidatos(1)
                .build();

        when(userAccountRepository.findByUsername(usuarioOrdinario.getUsername())).thenReturn(usuarioOrdinario);
        when(perteneceRepository.findAllConPartidoYCandidato()).thenReturn(List.of(ordinario, indigena));

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String vista = controller.mostrarTarjeton(authOrdinario, "ordinario", model, redirectAttributes);

        assertThat(vista).isEqualTo("votar");
        @SuppressWarnings("unchecked")
        var partidos = (java.util.Map<Partido, List<Pertenece>>) model.getAttribute("partidos");
        assertThat(partidos).containsOnlyKeys(partidoOrdinario);
        assertThat(partidos.get(partidoOrdinario)).containsExactly(ordinario);
    }

    @Test
    void registraVotoPreferenteYMarcaComoVotado() {
        when(userAccountRepository.findByUsername(usuarioOrdinario.getUsername())).thenReturn(usuarioOrdinario);

        Candidato candidato = Candidato.builder().cedula(999L).nombre("Candidato Preferente").circunscripcion("ORDINARIA").build();
        when(candidatoRepository.findById(999L)).thenReturn(Optional.of(candidato));
        when(eligeRepository.save(any(Elige.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountRepository.save(usuarioOrdinario)).thenReturn(usuarioOrdinario);

        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String vista = controller.emitirVoto(999L, null, redirect, authOrdinario);

        assertThat(vista).isEqualTo("redirect:/votante/gracias");
        assertThat(usuarioOrdinario.isHaVotado()).isTrue();
        verify(eligeRepository).save(any(Elige.class));
        verify(userAccountRepository).save(usuarioOrdinario);
    }

    @Test
    void registraVotoNoPreferenteTomandoPrimerCandidato() {
        when(userAccountRepository.findByUsername(usuarioOrdinario.getUsername())).thenReturn(usuarioOrdinario);

        Partido partido = Partido.builder().partidoId(15L).circunscripcion("ORDINARIA").tipoLista("cerrada").build();
        Candidato primero = Candidato.builder().cedula(111L).circunscripcion("ORDINARIA").build();
        Pertenece relacion = Pertenece.builder()
                .partido(partido)
                .candidato(primero)
                .ordenCandidatos(1)
                .build();

        when(perteneceRepository.findByPartido_PartidoIdOrderByOrdenCandidatosAsc(15L)).thenReturn(List.of(relacion));
        when(candidatoRepository.findById(111L)).thenReturn(Optional.of(primero));
        when(eligeRepository.save(any(Elige.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountRepository.save(usuarioOrdinario)).thenReturn(usuarioOrdinario);

        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String vista = controller.emitirVoto(null, 15L, redirect, authOrdinario);

        ArgumentCaptor<Elige> votoCaptor = ArgumentCaptor.forClass(Elige.class);
        verify(eligeRepository).save(votoCaptor.capture());
        assertThat(vista).isEqualTo("redirect:/votante/gracias");
        assertThat(votoCaptor.getValue().getCandidato()).isEqualTo(primero);
        assertThat(usuarioOrdinario.isHaVotado()).isTrue();
    }

    @Test
    void impideSegundoVotoEnEmision() {
        usuarioOrdinario.setHaVotado(true);
        when(userAccountRepository.findByUsername(usuarioOrdinario.getUsername())).thenReturn(usuarioOrdinario);

        RedirectAttributes redirect = new RedirectAttributesModelMap();
        String vista = controller.emitirVoto(1L, null, redirect, authOrdinario);

        assertThat(vista).isEqualTo("redirect:/votante/gracias");
        assertThat(redirect.getFlashAttributes()).containsEntry("mensaje", " Ya votaste.");
        verifyNoInteractions(candidatoRepository);
        verify(userAccountRepository, never()).save(usuarioOrdinario);
    }
}

