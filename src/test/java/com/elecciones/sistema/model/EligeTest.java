package com.elecciones.sistema.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EligeTest {

    @Test
    void testConstructorCompletoYGetters() {
        Candidato candidato = Candidato.builder()
                .cedula(123L)
                .nombre("Juan")
                .circunscripcion("Nacional")
                .build();

        UUID uuid = UUID.randomUUID();

        Elige e = new Elige(
                10L,
                uuid,
                candidato
        );

        assertThat(e.getIdElige()).isEqualTo(10L);
        assertThat(e.getHashVotante()).isEqualTo(uuid);
        assertThat(e.getCandidato()).isSameAs(candidato);
    }

    @Test
    void testSetters() {
        Elige e = new Elige();

        UUID uuid = UUID.randomUUID();

        Candidato candidato = Candidato.builder()
                .cedula(999L)
                .nombre("Maria")
                .circunscripcion("Local")
                .build();

        e.setIdElige(5L);
        e.setHashVotante(uuid);
        e.setCandidato(candidato);

        assertThat(e.getIdElige()).isEqualTo(5L);
        assertThat(e.getHashVotante()).isEqualTo(uuid);
        assertThat(e.getCandidato()).isSameAs(candidato);
    }

    @Test
    void testBuilder() {
        UUID uuid = UUID.randomUUID();

        Candidato candidato = Candidato.builder()
                .cedula(456L)
                .nombre("Carlos")
                .circunscripcion("Afro")
                .build();

        Elige e = Elige.builder()
                .idElige(20L)
                .hashVotante(uuid)
                .candidato(candidato)
                .build();

        assertThat(e.getIdElige()).isEqualTo(20L);
        assertThat(e.getHashVotante()).isEqualTo(uuid);
        assertThat(e.getCandidato()).isSameAs(candidato);
    }

    @Test
    void testNotEqualsByDefault() {
        Elige e1 = Elige.builder()
                .idElige(1L)
                .hashVotante(UUID.randomUUID())
                .build();

        Elige e2 = Elige.builder()
                .idElige(1L)
                .hashVotante(UUID.randomUUID())
                .build();

        // Como no hay @EqualsAndHashCode, dos instancias distintas NO son iguales
        assertThat(e1).isNotEqualTo(e2);
    }

    @Test
    void testToStringNotNull() {
        Elige e = Elige.builder()
                .idElige(3L)
                .hashVotante(UUID.randomUUID())
                .build();

        assertThat(e.toString()).isNotNull();
    }
}
