package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.Elige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EligeRepository extends JpaRepository<Elige, Long> {

    // 🔹 Verifica si un votante (hash) ya emitió su voto
    boolean existsByHashVotante(UUID hashVotante);

    // 🔹 Conteo total de votos emitidos por candidato
    @Query("""
        SELECT e.candidato.cedula, e.candidato.nombre, COUNT(e)
        FROM Elige e
        GROUP BY e.candidato.cedula, e.candidato.nombre
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> contarVotosPorCandidato();
    // 🔹 Conteo total de votos emitidos por partido (usando solo Elige)
    @Query("""
    SELECT p.nombre, COUNT(e.idElige)
    FROM Elige e
    JOIN e.candidato c
    JOIN c.pertenece pe
    JOIN pe.partido p
    GROUP BY p.nombre
    ORDER BY COUNT(e.idElige) DESC
""")
    List<Object[]> contarVotosPorPartido();

}