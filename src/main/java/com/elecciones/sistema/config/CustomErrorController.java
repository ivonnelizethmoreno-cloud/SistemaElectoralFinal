package com.elecciones.sistema.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        if (throwable != null) {
            System.err.println("🔥 ERROR DETECTADO:");
            throwable.printStackTrace(); // 💥 Esto imprimirá el error real
        } else {
            System.err.println("⚠️ Error genérico sin excepción adjunta");
        }
        return "error";
    }
}
