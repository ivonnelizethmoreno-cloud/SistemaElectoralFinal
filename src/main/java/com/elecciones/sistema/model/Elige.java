package com.elecciones.sistema.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "elige", schema = "votaciones_senado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Elige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_elige")
    private Long idElige;

    /*@Column(name = "hash_votante", nullable = false, unique = true)*/
    @Column(name = "hash_votante", nullable = false)
    private UUID hashVotante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", referencedColumnName = "cedula_candidato", nullable = false)
    private Candidato candidato;

}
