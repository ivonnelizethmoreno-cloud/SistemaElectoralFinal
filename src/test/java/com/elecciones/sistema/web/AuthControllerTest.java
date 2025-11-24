package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU 1: Autenticación
 * Pruebas unitarias para:
 *  - Vista de login (AuthController)
 *  - Flujo de autenticación real (HomeController)
 *  - Redirecciones por rol
 *  - Comportamiento según haya votado o no
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    // --- Mocks ---
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private Authentication auth;

    // --- Controladores reales ---
    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private HomeController homeController;

    // --- Usuarios simulados ---
    private UserAccount admin;
    private UserAccount votanteNoVota;
    private UserAccount votanteYaVoto;

    @BeforeEach
    void setUp() {
        admin = new UserAccount();
        admin.setUsername("admin01");
        admin.setRole("ADMIN");

        votanteNoVota = new UserAccount();
        votanteNoVota.setUsername("votante01");
        votanteNoVota.setRole("VOTANTE");
        votanteNoVota.setHaVotado(false);

        votanteYaVoto = new UserAccount();
        votanteYaVoto.setUsername("votante02");
        votanteYaVoto.setRole("VOTANTE");
        votanteYaVoto.setHaVotado(true);
    }

    // =====================================================================
    // 1. AuthController retorna la página de login
    // =====================================================================
    @Test
    void debeRetornarVistaLogin() {
        String vista = authController.loginPage();
        assertEquals("login", vista);
    }

    // =====================================================================
    // 2. HomeController: Usuario no autenticado → "home"
    // =====================================================================
    @Test
    void debeEnviarAHomeSiNoHayAuth() {
        Model model = new ExtendedModelMap();
        String vista = homeController.home(null, model);
        assertEquals("home", vista);
    }

    // =====================================================================
    // 3. Usuario no encontrado en BD → error
    // =====================================================================
    @Test
    void debeMostrarErrorSiUsuarioNoExiste() {
        when(auth.getName()).thenReturn("fantasma");
        when(userAccountRepository.findByUsername("fantasma")).thenReturn(null);

        Model model = new ExtendedModelMap();
        String vista = homeController.home(auth, model);

        assertEquals("error", vista);
        assertEquals("Usuario no encontrado", model.getAttribute("mensaje"));
    }

    // =====================================================================
    // 4. ADMIN → redirige a panel admin
    // =====================================================================
    @Test
    void adminDebeIrAlPanelAdmin() {
        when(auth.getName()).thenReturn("admin01");
        when(userAccountRepository.findByUsername("admin01")).thenReturn(admin);

        Model model = new ExtendedModelMap();
        String vista = homeController.home(auth, model);

        assertEquals("admin", vista);
    }

    // =====================================================================
    // 5. VOTANTE NO ha votado → redirige a /votar
    // =====================================================================
    @Test
    void votanteDebeIrATarjetonSiNoHaVotado() {
        when(auth.getName()).thenReturn("votante01");
        when(userAccountRepository.findByUsername("votante01")).thenReturn(votanteNoVota);

        Model model = new ExtendedModelMap();
        String vista = homeController.home(auth, model);

        assertEquals("redirect:/votar", vista);
    }

    // =====================================================================
    // 6. VOTANTE ya votó → va a /gracias
    // =====================================================================
    @Test
    void votanteYaVotoDebeIrAGracias() {
        when(auth.getName()).thenReturn("votante02");
        when(userAccountRepository.findByUsername("votante02")).thenReturn(votanteYaVoto);

        Model model = new ExtendedModelMap();
        String vista = homeController.home(auth, model);

        assertEquals("redirect:/gracias", vista);
    }

    // =====================================================================
    // 7. volver-inicio: ADMIN vuelve a admin
    // =====================================================================
    @Test
    void volverInicioAdmin() {
        when(auth.getName()).thenReturn("admin01");
        when(userAccountRepository.findByUsername("admin01")).thenReturn(admin);

        Model model = new ExtendedModelMap();
        String vista = homeController.volverInicio(auth, model);

        assertEquals("admin", vista);
    }

    // =====================================================================
    // 8. volver-inicio: votante no vota → /votar
    // =====================================================================
    @Test
    void volverInicioVotanteSinVotar() {
        when(auth.getName()).thenReturn("votante01");
        when(userAccountRepository.findByUsername("votante01")).thenReturn(votanteNoVota);

        Model model = new ExtendedModelMap();
        String vista = homeController.volverInicio(auth, model);

        assertEquals("redirect:/votar", vista);
    }

    // =====================================================================
    // 9. volver-inicio: votante ya votó → /gracias
    // =====================================================================
    @Test
    void volverInicioVotanteQueYaVoto() {
        when(auth.getName()).thenReturn("votante02");
        when(userAccountRepository.findByUsername("votante02")).thenReturn(votanteYaVoto);

        Model model = new ExtendedModelMap();
        String vista = homeController.volverInicio(auth, model);

        assertEquals("redirect:/gracias", vista);
    }

    // =====================================================================
    // 10. /gracias devuelve vista correcta
    // =====================================================================
    @Test
    void vistaGraciasCorrecta() {
        assertEquals("gracias", homeController.gracias());
    }
}



