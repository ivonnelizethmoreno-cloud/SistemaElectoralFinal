package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    /**
     * 🔹 Carga todos los partidos junto con su lista de pertenencias
     *    (evita LazyInitializationException)
     */
    @Query("SELECT DISTINCT p FROM Partido p " +
            "LEFT JOIN FETCH p.pertenece per " +
            "LEFT JOIN FETCH per.candidato")
    List<Partido> findAllWithPertenece();

    /**
     * 🔹 Buscar un partido por nombre sin importar mayúsculas/minúsculas
     *    (NECESARIO para la carga de Partidos en el PanelController)
     */
    Optional<Partido> findByNombreIgnoreCase(String nombre);
}
