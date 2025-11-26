package com.elecciones.sistema.security;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DBUserDetailsServiceTest {

    private UserAccountRepository repository;
    private DBUserDetailsService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserAccountRepository.class);
        service = new DBUserDetailsService(repository);
    }

    @Test
    void loadUserByUsername_retornaDetallesConRol() {
        UserAccount account = new UserAccount();
        account.setUsername("juan");
        account.setPassword("encodedPass");
        account.setRole("votante");

        when(repository.findByUsername("juan")).thenReturn(account);

        UserDetails details = service.loadUserByUsername("juan");

        assertThat(details.getUsername()).isEqualTo("juan");
        assertThat(details.getPassword()).isEqualTo("encodedPass");
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_VOTANTE"));
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        when(repository.findByUsername("desconocido")).thenReturn(null);

        assertThatThrownBy(() -> service.loadUserByUsername("desconocido"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }
+}
