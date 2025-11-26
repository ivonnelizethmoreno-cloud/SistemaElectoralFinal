package com.elecciones.sistema.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PerteneceTest {

    @Test
    void testConstructorCompletoYGetters() {
        Partido partido = new Partido(1L, "Partido A", "cerrada", "ordinaria", new ArrayList<>());
        Candidato candidato = new Candidato(100L, "Juan Pérez", "ordinaria", new ArrayList<>());

        Pertenece pertenece = new Pertenece(
                partido,
                candidato,
                5
        );

        assertThat(pertenece.getPartido()).isEqualTo(partido);
        assertThat(pertenece.getCandidato()).isEqualTo(candidato);
        assertThat(pertenece.getOrdenCandidatos()).isEqualTo(5);
    }

    @Test
    void testSetters() {
        Pertenece pertenece = new Pertenece();

        Partido partido = new Partido(2L, "Partido B", "abierta", "indigena", new ArrayList<>());
        Candidato candidato = new Candidato(200L, "Carlos López", "indigena", new ArrayList<>());

        pertenece.setPartido(partido);
        pertenece.setCandidato(candidato);
        pertenece.setOrdenCandidatos(3);

        assertThat(pertenece.getPartido()).isEqualTo(partido);
        assertThat(pertenece.getCandidato()).isEqualTo(candidato);
        assertThat(pertenece.getOrdenCandidatos()).isEqualTo(3);
    }

    @Test
    void testBuilder() {
        Partido partido = new Partido(3L, "Partido C", "cerrada", "ordinaria", new ArrayList<>());
        Candidato candidato = new Candidato(300L, "Maria Ruiz", "ordinaria", new ArrayList<>());

        Pertenece pertenece = Pertenece.builder()
                .partido(partido)
                .candidato(candidato)
                .ordenCandidatos(10)
                .build();

        assertThat(pertenece.getPartido()).isEqualTo(partido);
        assertThat(pertenece.getCandidato()).isEqualTo(candidato);
        assertThat(pertenece.getOrdenCandidatos()).isEqualTo(10);
    }

    @Test
    void testPKEqualsAndHashCode() {
        Pertenece.PK pk1 = new Pertenece.PK(1L, 100L);
        Pertenece.PK pk2 = new Pertenece.PK(1L, 100L);
        Pertenece.PK pk3 = new Pertenece.PK(2L, 200L);

        // pk1 y pk2 deben ser iguales (mismos valores)
        assertThat(pk1).isEqualTo(pk2);
        assertThat(pk1.hashCode()).isEqualTo(pk2.hashCode());

        // pk1 y pk3 deben ser diferentes
        assertThat(pk1).isNotEqualTo(pk3);
        assertThat(pk1.hashCode()).isNotEqualTo(pk3.hashCode());
    }

    @Test
    void testPKGettersAndSetters() {
        Pertenece.PK pk = new Pertenece.PK();

        pk.setPartido(5L);
        pk.setCandidato(50L);

        assertThat(pk.getPartido()).isEqualTo(5L);
        assertThat(pk.getCandidato()).isEqualTo(50L);
    }
}
