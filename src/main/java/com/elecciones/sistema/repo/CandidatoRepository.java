package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    // 🔹 Buscar candidato por nombre (ignora mayúsculas/minúsculas)
    Candidato findByNombreIgnoreCase(String nombre);

    // 🔹 Buscar candidato por cédula
    Candidato findByCedula(Long cedula);

    // 🔹 Buscar candidatos por circunscripción
    List<Candidato> findAllByCircunscripcionIgnoreCase(String circunscripcion);
}
