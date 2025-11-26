package com.elecciones.sistema.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidatoTest {

    @Test
    void testConstructorCompletoYGetters() {
        List<Pertenece> lista = new ArrayList<>();

        Candidato c = new Candidato(
                123L,
                "Juan Perez",
                "Nacional",
                lista
        );

        assertThat(c.getCedula()).isEqualTo(123L);
        assertThat(c.getNombre()).isEqualTo("Juan Perez");
        assertThat(c.getCircunscripcion()).isEqualTo("Nacional");
        assertThat(c.getPertenece()).isSameAs(lista);
    }

    @Test
    void testSetters() {
        Candidato c = new Candidato();

        c.setCedula(456L);
        c.setNombre("Maria Lopez");
        c.setCircunscripcion("Indigena");

        List<Pertenece> lista = new ArrayList<>();
        c.setPertenece(lista);

        assertThat(c.getCedula()).isEqualTo(456L);
        assertThat(c.getNombre()).isEqualTo("Maria Lopez");
        assertThat(c.getCircunscripcion()).isEqualTo("Indigena");
        assertThat(c.getPertenece()).isSameAs(lista);
    }

    @Test
    void testBuilder() {
        List<Pertenece> lista = new ArrayList<>();

        Candidato c = Candidato.builder()
                .cedula(789L)
                .nombre("Carlos Ruiz")
                .circunscripcion("Afrodescendiente")
                .pertenece(lista)
                .build();

        assertThat(c.getCedula()).isEqualTo(789L);
        assertThat(c.getNombre()).isEqualTo("Carlos Ruiz");
        assertThat(c.getCircunscripcion()).isEqualTo("Afrodescendiente");
        assertThat(c.getPertenece()).isSameAs(lista);
    }

    @Test
    void testEqualsYhashCode() {
        List<Pertenece> lista = new ArrayList<>();

        Candidato c1 = Candidato.builder()
                .cedula(1L)
                .nombre("A")
                .circunscripcion("X")
                .pertenece(lista)
                .build();

        Candidato c2 = Candidato.builder()
                .cedula(1L)
                .nombre("A")
                .circunscripcion("X")
                .pertenece(lista)
                .build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void testToString() {
        Candidato c = Candidato.builder()
                .cedula(3L)
                .nombre("Test")
                .circunscripcion("Y")
                .pertenece(new ArrayList<>()) // evita pertenece=null
                .build();

        String s = c.toString();
        assertThat(s).contains("cedula=3");
        assertThat(s).contains("nombre=Test");
        assertThat(s).contains("circunscripcion=Y");
    }
}
