package com.elecciones.sistema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_account", schema = "votaciones_senado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "correo_electronico")
    private String correoElectronico;

    @Column(name = "circunscripcion")
    private String circunscripcion;

    // ===========================================================
    // 🔹 Controla si el votante ya emitió su voto
    // ===========================================================
    @Column(name = "ha_votado", nullable = false)
    private boolean haVotado = false;

    // ===========================================================
    // 🔹 Estado del usuario (habilitado / deshabilitado)
    // ===========================================================
    @Column(name = "enabled")
    private boolean enabled = true;

    // ===========================================================
    // 🔹 Getters y Setters explícitos (para evitar conflictos con Lombok)
    // ===========================================================
    public boolean isHaVotado() {
        return haVotado;
    }

    public boolean getHaVotado() {
        return haVotado;
    }

    public void setHaVotado(boolean haVotado) {
        this.haVotado = haVotado;
    }
}