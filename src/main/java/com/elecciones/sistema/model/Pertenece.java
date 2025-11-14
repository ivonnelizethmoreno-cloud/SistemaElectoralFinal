package com.elecciones.sistema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pertenece", schema = "votaciones_senado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Pertenece.PK.class)
public class Pertenece {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_id", referencedColumnName = "partido_id", nullable = false)
    private Partido partido;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cedula_candidato", referencedColumnName = "cedula_candidato", nullable = false)
    private Candidato candidato;

    @Column(name = "orden_candidatos")
    private Integer ordenCandidatos;

    // 🔹 Clase interna para la PK compuesta
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Long partido;
        private Long candidato;
    }
}