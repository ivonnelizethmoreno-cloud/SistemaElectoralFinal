package com.elecciones.sistema.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PartidoTest {

    @Test
    void testConstructorCompletoYGetters() {
        List<Pertenece> lista = new ArrayList<>();

        Partido p = new Partido(
                10L,
                "Partido Verde",
                "cerrada",
                "ordinaria",
                lista
        );

        assertThat(p.getPartidoId()).isEqualTo(10L);
        assertThat(p.getNombre()).isEqualTo("Partido Verde");
        assertThat(p.getTipoLista()).isEqualTo("cerrada");
        assertThat(p.getCircunscripcion()).isEqualTo("ordinaria");
        assertThat(p.getPertenece()).isSameAs(lista);
    }

    @Test
    void testSetters() {
        Partido p = new Partido();

        p.setPartidoId(5L);
        p.setNombre("Partido Azul");
        p.setTipoLista("abierta");
        p.setCircunscripcion("indigena");

        List<Pertenece> lista = new ArrayList<>();
        p.setPertenece(lista);

        assertThat(p.getPartidoId()).isEqualTo(5L);
        assertThat(p.getNombre()).isEqualTo("Partido Azul");
        assertThat(p.getTipoLista()).isEqualTo("abierta");
        assertThat(p.getCircunscripcion()).isEqualTo("indigena");
        assertThat(p.getPertenece()).isSameAs(lista);
    }

    @Test
    void testBuilder() {
        List<Pertenece> lista = new ArrayList<>();

        Partido p = Partido.builder()
                .partidoId(20L)
                .nombre("Partido Amarillo")
                .tipoLista("cerrada")
                .circunscripcion("ordinaria")
                .pertenece(lista)
                .build();

        assertThat(p.getPartidoId()).isEqualTo(20L);
        assertThat(p.getNombre()).isEqualTo("Partido Amarillo");
        assertThat(p.getTipoLista()).isEqualTo("cerrada");
        assertThat(p.getCircunscripcion()).isEqualTo("ordinaria");
        assertThat(p.getPertenece()).isSameAs(lista);
    }

    @Test
    void testEqualsYHashCode() {
        Partido p1 = Partido.builder()
                .partidoId(1L)
                .nombre("X")
                .tipoLista("abierta")
                .circunscripcion("ordinaria")
                .build();

        Partido p2 = Partido.builder()
                .partidoId(1L)
                .nombre("X")
                .tipoLista("abierta")
                .circunscripcion("ordinaria")
                .build();

        // Lombok no implementa equals/hashCode → solo verifica que no sean iguales por referencia
        assertThat(p1).isNotEqualTo(p2);
        assertThat(p1.hashCode()).isNotEqualTo(p2.hashCode());
    }

    @Test
    void testToString() {
        Partido p = Partido.builder()
                .partidoId(3L)
                .nombre("Test Partido")
                .tipoLista("cerrada")
                .circunscripcion("ordinaria")
                .build();

        String s = p.toString();

        assertThat(s).contains("partidoId=3");
        assertThat(s).contains("nombre=Test Partido");
        assertThat(s).contains("tipoLista=cerrada");
        assertThat(s).contains("circunscripcion=ordinaria");
    }
}
