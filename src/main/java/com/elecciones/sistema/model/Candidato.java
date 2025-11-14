package com.elecciones.sistema.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "candidatos", schema = "votaciones_senado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidato {

    @Id
    @Column(name = "cedula_candidato")
    private Long cedula;

    @Column(name = "nombre_candidato", nullable = false)
    private String nombre;

    @Column(name = "circunscripcion_candidato", nullable = false)
    private String circunscripcion;

    // 🔹 Relación inversa hacia Pertenece
    @OneToMany(mappedBy = "candidato", fetch = FetchType.LAZY)
    private List<Pertenece> pertenece;
}
