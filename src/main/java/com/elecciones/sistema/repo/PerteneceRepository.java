package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.Pertenece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerteneceRepository extends JpaRepository<Pertenece, Pertenece.PK> {

    // ===========================================================
    // 🔹 CONSULTAS EXISTENTES (mantienen el comportamiento previo)
    // ===========================================================

    // 🔹 Devuelve las relaciones pertenecientes a un partido (ordenadas por ordenCandidatos)
    List<Pertenece> findByPartido_PartidoIdOrderByOrdenCandidatosAsc(Long partidoId);

    // 🔹 Busca una relación específica partido–candidato
    Optional<Pertenece> findByPartido_PartidoIdAndCandidato_Cedula(Long partidoId, Long cedulaCandidato);

    // 🔹 Permite buscar relaciones por nombre del partido
    @Query("""
        SELECT p FROM Pertenece p 
        WHERE LOWER(TRIM(p.partido.nombre)) = LOWER(TRIM(:nombre)) 
        ORDER BY p.ordenCandidatos ASC
    """)
    List<Pertenece> findByNombrePartido(String nombre);

    // ===========================================================
    // 🔹 NUEVA CONSULTA SEGURA (evita LazyInitializationException)
    // ===========================================================
    // Esta consulta carga en una sola operación los partidos y candidatos
    // asociados, asegurando que Hibernate tenga todo disponible para Thymeleaf.
    @Query("""
        SELECT p FROM Pertenece p
        JOIN FETCH p.partido
        JOIN FETCH p.candidato
        ORDER BY p.partido.partidoId, p.ordenCandidatos
    """)
    List<Pertenece> findAllConPartidoYCandidato();
}