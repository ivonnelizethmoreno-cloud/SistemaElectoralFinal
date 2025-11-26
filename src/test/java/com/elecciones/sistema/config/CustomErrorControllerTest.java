package com.elecciones.sistema.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomErrorControllerTest {

    private final CustomErrorController controller = new CustomErrorController();

    @Test
    void handleError_conExcepcionDevuelveVista() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("jakarta.servlet.error.exception"))
                .thenReturn(new IllegalStateException("fallo"));

        String view = controller.handleError(request);

        assertThat(view).isEqualTo("error");
    }

    @Test
    void handleError_sinExcepcionDevuelveVista() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("jakarta.servlet.error.exception"))
                .thenReturn(null);

        String view = controller.handleError(request);

        assertThat(view).isEqualTo("error");
    }
}
