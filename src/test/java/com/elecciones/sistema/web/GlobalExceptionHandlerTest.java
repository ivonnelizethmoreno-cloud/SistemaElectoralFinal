package com.elecciones.sistema.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    // =====================================================
    // TEST 1: handleMissingResource ignora favicon.ico
    // =====================================================
    @Test
    void handleMissingResource_ignoraFavicon_noLog() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/favicon.ico");

        NoResourceFoundException ex =
                new NoResourceFoundException(HttpMethod.GET, "/favicon.ico");

        handler.handleMissingResource(ex, request);
    }

    // =====================================================
    // TEST 2: handleMissingResource para otro recurso
    // =====================================================
    @Test
    void handleMissingResource_noFavicon_noRetornaVista() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/img/no-existe.png");

        NoResourceFoundException ex =
                new NoResourceFoundException(HttpMethod.GET, "/img/no-existe.png");

        handler.handleMissingResource(ex, request);
    }

    // =====================================================
    // TEST 3: Captura de Exception general: con mensaje
    // =====================================================
    @Test
    void handleAnyException_retornaError_conMensaje() {

        Exception ex = new Exception("Algo falló");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/ruta/prueba");

        Model model = new ConcurrentModel();

        String view = handler.handleAnyException(ex, request, model);

        assertThat(view).isEqualTo("error");
        assertThat(model.getAttribute("titulo")).isEqualTo("Se produjo un error");
        assertThat(model.getAttribute("mensaje")).isEqualTo("Algo falló");
        assertThat(model.getAttribute("errorPath")).isEqualTo("/ruta/prueba");
    }

    // =====================================================
    // TEST 4: Exception general con mensaje NULL
    // =====================================================
    @Test
    void handleAnyException_mensajeNull() {

        Exception ex = new Exception((String) null); // mensaje null
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/x");

        Model model = new ConcurrentModel();

        String view = handler.handleAnyException(ex, request, model);

        assertThat(view).isEqualTo("error");
        assertThat(model.getAttribute("mensaje"))
                .isEqualTo("Error interno del servidor.");
    }
}
