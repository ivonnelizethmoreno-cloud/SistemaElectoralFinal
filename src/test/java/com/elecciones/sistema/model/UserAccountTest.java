package com.elecciones.sistema.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

    @Test
    void testConstructorCompletoYGetters() {
        UserAccount user = new UserAccount(
                1L,
                "usuario1",
                "pass123",
                "ROLE_USER",
                "Juan Pérez",
                "correo@test.com",
                "nacional",
                true,
                false
        );

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("usuario1");
        assertThat(user.getPassword()).isEqualTo("pass123");
        assertThat(user.getRole()).isEqualTo("ROLE_USER");
        assertThat(user.getNombreUsuario()).isEqualTo("Juan Pérez");
        assertThat(user.getCorreoElectronico()).isEqualTo("correo@test.com");
        assertThat(user.getCircunscripcion()).isEqualTo("nacional");

        assertThat(user.isHaVotado()).isTrue();
        assertThat(user.getHaVotado()).isTrue(); // ambos deben ser true

        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void testSetters() {
        UserAccount user = new UserAccount();

        user.setId(10L);
        user.setUsername("admin");
        user.setPassword("adminpass");
        user.setRole("ROLE_ADMIN");
        user.setNombreUsuario("Administrador");
        user.setCorreoElectronico("admin@test.com");
        user.setCircunscripcion("especial");

        user.setHaVotado(true);
        user.setEnabled(true);

        assertThat(user.getId()).isEqualTo(10L);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("adminpass");
        assertThat(user.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(user.getNombreUsuario()).isEqualTo("Administrador");
        assertThat(user.getCorreoElectronico()).isEqualTo("admin@test.com");
        assertThat(user.getCircunscripcion()).isEqualTo("especial");

        assertThat(user.getHaVotado()).isTrue();
        assertThat(user.isHaVotado()).isTrue();

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testBuilder() {
        UserAccount user = UserAccount.builder()
                .id(5L)
                .username("builderUser")
                .password("builderPass")
                .role("ROLE_USER")
                .nombreUsuario("Builder Test")
                .correoElectronico("builder@test.com")
                .circunscripcion("territorial")
                .haVotado(false)
                .enabled(true)
                .build();

        assertThat(user.getId()).isEqualTo(5L);
        assertThat(user.getUsername()).isEqualTo("builderUser");
        assertThat(user.getPassword()).isEqualTo("builderPass");
        assertThat(user.getRole()).isEqualTo("ROLE_USER");
        assertThat(user.getNombreUsuario()).isEqualTo("Builder Test");
        assertThat(user.getCorreoElectronico()).isEqualTo("builder@test.com");
        assertThat(user.getCircunscripcion()).isEqualTo("territorial");

        assertThat(user.isHaVotado()).isFalse();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testToString() {
        UserAccount user = UserAccount.builder()
                .id(7L)
                .username("testUser")
                .role("ROLE_USER")
                .build();

        String s = user.toString();

        // Lombok @ToString incluye los campos automáticamente
        assertThat(s).contains("id=7");
        assertThat(s).contains("username=testUser");
        assertThat(s).contains("role=ROLE_USER");
    }
}
