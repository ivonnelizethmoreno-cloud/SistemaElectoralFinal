package com.elecciones.sistema.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "partido", schema = "votaciones_senado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partido {

    // ===========================================================
    // 🔹 CLAVE PRIMARIA AUTOINCREMENTAL
    // ===========================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partido_id")
    private Long partidoId; // Usa Long (PostgreSQL lo maneja mejor que Integer)

    // ===========================================================
    // 🔹 ATRIBUTOS PRINCIPALES
    // ===========================================================
    @Column(name = "nombre_partido", nullable = false, unique = true)
    private String nombre;  // Nombre_Partido

    @Column(name = "tipo_lista", nullable = false)
    private String tipoLista; // 'abierta' o 'cerrada'

    @Column(name = "circunscripcion_partido", nullable = false)
    private String circunscripcion; // ordinaria / indígena

    // ===========================================================
    // 🔹 RELACIÓN CON CANDIDATOS (vía Pertenece)
    // ===========================================================
    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pertenece> pertenece;
}
