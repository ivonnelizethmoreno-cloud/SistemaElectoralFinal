package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    @Mock
    private UserAccountRepository repo;

    @Mock
    private Authentication auth;

    @InjectMocks
    private HomeController controller;

    @Test
    void testHomeControllerCompleto() {

        // ----------------------------
        // 1. Usuario NO autenticado
        // ----------------------------
        Model model = new ExtendedModelMap();
        String r1 = controller.home(null, model);
        assertThat(r1).isEqualTo("home");

        // ----------------------------
        // 2. Usuario no encontrado
        // ----------------------------
        when(auth.getName()).thenReturn("xuser");
        when(repo.findByUsername("xuser")).thenReturn(null);

        String r2 = controller.home(auth, model);
        assertThat(r2).isEqualTo("error");
        assertThat(model.getAttribute("mensaje")).isEqualTo("Usuario no encontrado");

        // ----------------------------
        // 3. ADMIN
        // ----------------------------
        UserAccount admin = new UserAccount();
        admin.setRole("ADMIN");
        when(repo.findByUsername("xuser")).thenReturn(admin);

        String r3 = controller.home(auth, new ExtendedModelMap());
        assertThat(r3).isEqualTo("admin");

        // ----------------------------
        // 4. VOTANTE sin votar
        // ----------------------------
        UserAccount votante = new UserAccount();
        votante.setRole("VOTANTE");
        votante.setHaVotado(false);
        when(repo.findByUsername("xuser")).thenReturn(votante);

        String r4 = controller.home(auth, new ExtendedModelMap());
        assertThat(r4).isEqualTo("redirect:/votar");

        // ----------------------------
        // 5. VOTANTE que YA votó
        // ----------------------------
        votante.setHaVotado(true);
        when(repo.findByUsername("xuser")).thenReturn(votante);

        String r5 = controller.home(auth, new ExtendedModelMap());
        assertThat(r5).isEqualTo("redirect:/gracias");

        // ----------------------------
        // 6. Rol desconocido
        // ----------------------------
        UserAccount otro = new UserAccount();
        otro.setRole("OTRO");
        when(repo.findByUsername("xuser")).thenReturn(otro);

        String r6 = controller.home(auth, new ExtendedModelMap());
        assertThat(r6).isEqualTo("home");

        // -----------------------------------
        // 7. volver-inicio: no autenticado
        // -----------------------------------
        String r7 = controller.volverInicio(null, new ExtendedModelMap());
        assertThat(r7).isEqualTo("redirect:/login");

        // -----------------------------------
        // 8. volver-inicio: usuario no existe
        // -----------------------------------
        when(repo.findByUsername("xuser")).thenReturn(null);
        Model m8 = new ExtendedModelMap();
        String r8 = controller.volverInicio(auth, m8);
        assertThat(r8).isEqualTo("error");
        assertThat(m8.getAttribute("mensaje")).isEqualTo("Usuario no encontrado");

        // -----------------------------------
        // 9. volver-inicio: ADMIN
        // -----------------------------------
        when(repo.findByUsername("xuser")).thenReturn(admin);
        String r9 = controller.volverInicio(auth, new ExtendedModelMap());
        assertThat(r9).isEqualTo("admin");

        // -----------------------------------
        // 10. volver-inicio: VOTANTE sin votar
        // -----------------------------------
        votante.setHaVotado(false);
        when(repo.findByUsername("xuser")).thenReturn(votante);

        String r10 = controller.volverInicio(auth, new ExtendedModelMap());
        assertThat(r10).isEqualTo("redirect:/votar");

        // -----------------------------------
        // 11. volver-inicio: VOTANTE ya votó
        // -----------------------------------
        votante.setHaVotado(true);
        when(repo.findByUsername("xuser")).thenReturn(votante);

        String r11 = controller.volverInicio(auth, new ExtendedModelMap());
        assertThat(r11).isEqualTo("redirect:/gracias");

        // -----------------------------------
        // 12. Gracias
        // -----------------------------------
        String r12 = controller.gracias();
        assertThat(r12).isEqualTo("gracias");
    }
}
