package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserAccountRepository repo;

    /**
     * Página principal: redirige según el rol y estado de voto.
     */
    @GetMapping({"/", "/home"})
    public String home(Authentication auth, Model model) {
        if (auth == null) {
            // Usuario no autenticado → página pública
            return "home";
        }

        String username = auth.getName();
        UserAccount user = repo.findByUsername(username);

        if (user == null) {
            model.addAttribute("mensaje", "Usuario no encontrado");
            return "error";
        }

        // ADMIN → panel de administración
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "admin";
        }

        // VOTANTE → redirige automáticamente al flujo de votación
        if ("VOTANTE".equalsIgnoreCase(user.getRole())) {

            // Si ya votó → va a página de agradecimiento
            if (Boolean.TRUE.equals(user.getHaVotado())) {
                return "redirect:/gracias";
            }

            // Si no ha votado → va directamente al tarjetón controlado por VotoController
            return "redirect:/votar";
        }

        // Rol desconocido → home genérico
        return "home";
    }

    /**
     * Permite volver al inicio sin perder sesión.
     */
    @GetMapping("/volver-inicio")
    public String volverInicio(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/login";
        }

        String username = auth.getName();
        UserAccount user = repo.findByUsername(username);

        if (user == null) {
            model.addAttribute("mensaje", "Usuario no encontrado");
            return "error";
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "admin";
        }

        if ("VOTANTE".equalsIgnoreCase(user.getRole())) {
            return Boolean.TRUE.equals(user.getHaVotado())
                    ? "redirect:/gracias"
                    : "redirect:/votar";
        }

        return "home";
    }

    /**
     * Página de agradecimiento tras emitir el voto.
     */
    @GetMapping("/gracias")
    public String gracias() {
        return "gracias";
    }
}
