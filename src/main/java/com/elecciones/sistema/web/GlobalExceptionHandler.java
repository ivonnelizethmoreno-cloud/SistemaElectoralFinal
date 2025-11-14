package com.elecciones.sistema.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // 🚫 Ignorar favicon.ico y recursos inexistentes
    // =====================================================
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleMissingResource(NoResourceFoundException ex, HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Evita llenar el log con favicon.ico
        if (!uri.endsWith("favicon.ico")) {
            log.warn("⚠️ Recurso no encontrado: {}", uri);
        }
        // No retornamos vista porque son recursos estáticos (no errores de aplicación)
    }

    // =====================================================
    // ❌ Captura de errores generales
    // =====================================================
    @ExceptionHandler(Exception.class)
    public String handleAnyException(Exception ex, HttpServletRequest request, Model model) {
        log.error("❌ Error no manejado:", ex);

        model.addAttribute("titulo", "Se produjo un error");
        model.addAttribute("mensaje",
                ex.getMessage() == null
                        ? "Error interno del servidor."
                        : ex.getMessage());
        model.addAttribute("errorPath", request.getRequestURI());

        return "error"; // Renderiza la plantilla error.html
    }
}
