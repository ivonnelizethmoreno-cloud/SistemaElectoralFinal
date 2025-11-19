package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.Elige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EligeRepository extends JpaRepository<Elige, Long> {

    boolean existsByHashVotante(UUID hashVotante);

    // 🔥 VOTOS POR CANDIDATO – seguro
    @Query("""
        SELECT c.cedula, c.nombre, COUNT(e.idElige)
        FROM Candidato c
        LEFT JOIN Elige e ON e.candidato = c
        GROUP BY c.cedula, c.nombre
        ORDER BY COUNT(e.idElige) DESC
    """)
    List<Object[]> contarVotosPorCandidato();

    // 🔥 VOTOS POR PARTIDO – seguro
    @Query("""
        SELECT p.nombre, COUNT(e.idElige)
        FROM Partido p
        LEFT JOIN p.pertenece pe
        LEFT JOIN pe.candidato c
        LEFT JOIN Elige e ON e.candidato = c
        GROUP BY p.nombre
        ORDER BY COUNT(e.idElige) DESC
    """)
    List<Object[]> contarVotosPorPartido();

    // 🟨 NUEVO: VOTOS SOLO PARA CANDIDATOS INDÍGENAS
    @Query("""
        SELECT c.nombre, COUNT(e.idElige)
        FROM Candidato c
        LEFT JOIN Elige e ON e.candidato = c
        WHERE UPPER(c.circunscripcion) = 'INDIGENA'
        GROUP BY c.nombre
        ORDER BY COUNT(e.idElige) DESC
    """)
    List<Object[]> contarVotosIndigenasPorCandidato();
}
