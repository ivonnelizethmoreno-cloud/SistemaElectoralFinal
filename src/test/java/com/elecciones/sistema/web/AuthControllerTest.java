package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import com.elecciones.sistema.security.AuthController;
import com.elecciones.sistema.security.dto.LoginRequest;
import com.elecciones.sistema.security.dto.LoginResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU 1: Autenticación
 * Pruebas unitarias para validar:
 *  - Autenticación de votantes y administradores
 *  - Manejo de errores
 *  - Validación de registro en BD
 *  - Registro de intentos (mock básico)
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private AuthController AuthController;

    private UserAccount mockVoter;
    private UserAccount mockAdmin;

    @BeforeEach
    void setUp() {
        mockVoter = new UserAccount();
        mockVoter.setUsername("votante123");
        mockVoter.setPassword("clave123");
        mockVoter.setRol("VOTANTE");

        mockAdmin = new UserAccount();
        mockAdmin.setUsername("admin001");
        mockAdmin.setPassword("adminpass");
        mockAdmin.setRol("ADMIN");
    }

    // ========================================================
    // 1. AUTENTICACIÓN EXITOSA DE VOTANTE
    // ========================================================
    @Test
    void debePermitirIngresoVotanteRegistrado() {

        LoginRequest request = new LoginRequest("votante123", "clave123");

        when(userAccountRepository.findByUsername("votante123"))
                .thenReturn(mockVoter);

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VOTANTE", response.getBody().getRol());

        verify(userAccountRepository, times(1))
                .findByUsername("votante123");
    }

    // ========================================================
    // 2. AUTENTICACIÓN EXITOSA DE ADMINISTRADOR
    // ========================================================
    @Test
    void debePermitirIngresoAdministrador() {

        LoginRequest request = new LoginRequest("admin001", "adminpass");

        when(userAccountRepository.findByUsername("admin001"))
                .thenReturn(mockAdmin);

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("ADMIN", response.getBody().getRol());
    }

    // ========================================================
    // 3. USUARIO NO REGISTRADO NO PUEDE INGRESAR
    // ========================================================
    @Test
    void debeRechazarVotanteNoRegistrado() {

        LoginRequest request = new LoginRequest("desconocido", "pass");

        when(userAccountRepository.findByUsername("desconocido"))
                .thenReturn(null);

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Usuario no habilitado para votar",
                response.getBody().getMensaje());
    }

    // ========================================================
    // 4. CREDENCIALES INCORRECTAS
    // ========================================================
    @Test
    void debeRechazarCredencialesIncorrectas() {

        LoginRequest request = new LoginRequest("votante123", "claveIncorrecta");

        when(userAccountRepository.findByUsername("votante123"))
                .thenReturn(mockVoter);

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciales incorrectas",
                response.getBody().getMensaje());
    }

    // ========================================================
    // 5. VALIDAR DIFERENCIACIÓN DE ROLES
    // ========================================================
    @Test
    void debeDiferenciarRolVotante() {

        when(userAccountRepository.findByUsername("votante123"))
                .thenReturn(mockVoter);

        LoginRequest request = new LoginRequest("votante123", "clave123");

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals("VOTANTE", response.getBody().getRol());
    }

    @Test
    void debeDiferenciarRolAdministrador() {

        when(userAccountRepository.findByUsername("admin001"))
                .thenReturn(mockAdmin);

        LoginRequest request = new LoginRequest("admin001", "adminpass");

        ResponseEntity<LoginResponse> response =
                AuthController.login(request);

        assertEquals("ADMIN", response.getBody().getRol());
    }

    // ========================================================
    // 6. REGISTRO DE INTENTOS DE AUTENTICACIÓN (FALLIDOS)
    // ========================================================
    @Test
    void debeRegistrarIntentoFallido() {

        LoginRequest request = new LoginRequest("noExiste", "123");

        when(userAccountRepository.findByUsername("noExiste"))
                .thenReturn(null);

        AuthController.login(request);

        verify(userAccountRepository, times(1))
                .findByUsername("noExiste");
    }

    // ========================================================
    // 7. REGISTRO DE INTENTO EXITOSO
    // ========================================================
    @Test
    void debeRegistrarIntentoExitoso() {

        LoginRequest request = new LoginRequest("votante123", "clave123");

        when(userAccountRepository.findByUsername("votante123"))
                .thenReturn(mockVoter);

        AuthController.login(request);

        verify(userAccountRepository, times(1))
                .findByUsername("votante123");
    }
}

