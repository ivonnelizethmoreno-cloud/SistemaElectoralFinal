package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificadoControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CertificadoController certificadoController;

    private UserAccount mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserAccount();
        mockUser.setUsername("1234567890");
        mockUser.setNombreUsuario("Juan López");
    }

    // ========================================================
    // 1. CUANDO EL USUARIO AUTENTICADO EXISTE
    // ========================================================
    @Test
    void debeGenerarCertificadoConDatosDelUsuario() throws Exception {

        when(authentication.getName()).thenReturn("1234567890");
        when(userAccountRepository.findByUsername("1234567890"))
                .thenReturn(mockUser);

        ResponseEntity<InputStreamResource> response =
                certificadoController.descargarCertificado(authentication);

        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst("Content-Disposition")
                .contains("certificado_votacion.pdf"));
        assertNotNull(response.getBody());

        verify(userAccountRepository, times(1)).findByUsername("1234567890");
    }

    // ========================================================
    // 2. CUANDO NO HAY AUTENTICACIÓN
    // ========================================================
    @Test
    void debeGenerarCertificadoConValoresPorDefectoSiNoHayAuth() throws Exception {

        ResponseEntity<InputStreamResource> response =
                certificadoController.descargarCertificado(null);

        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst("Content-Disposition")
                .contains("certificado_votacion.pdf"));
        assertNotNull(response.getBody());

        verify(userAccountRepository, never()).findByUsername(anyString());
    }

    // ========================================================
    // 3. CUANDO HAY AUTH PERO EL USUARIO NO EXISTE EN BD
    // ========================================================
    @Test
    void debeGenerarCertificadoConValoresPorDefectoSiUsuarioNoExiste() throws Exception {

        when(authentication.getName()).thenReturn("00000000");
        when(userAccountRepository.findByUsername("00000000"))
                .thenReturn(null);

        ResponseEntity<InputStreamResource> response =
                certificadoController.descargarCertificado(authentication);

        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst("Content-Disposition")
                .contains("certificado_votacion.pdf"));
        assertNotNull(response.getBody());

        verify(userAccountRepository, times(1))
                .findByUsername("00000000");
    }
}
