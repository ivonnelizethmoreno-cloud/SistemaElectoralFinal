package com.elecciones.sistema.repo;

import com.elecciones.sistema.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * 🔹 Busca usuario ignorando mayúsculas/minúsculas y espacios.
     * Compatible tanto con PostgreSQL como con H2.
     */
    @Query("SELECT u FROM UserAccount u WHERE LOWER(TRIM(u.username)) = LOWER(TRIM(:username))")
    UserAccount findExistingUser(@Param("username") String username);

    /**
     * 🔹 Alias para mantener compatibilidad con clases existentes.
     * (SetupData, DBUserDetailsService, PanelController, etc.)
     */
    default UserAccount findByUsername(String username) {
        return findExistingUser(username);
    }

    /**
     * 🔹 Cuenta cuántos votantes ya ejercieron su voto (ha_votado = true)
     */
    long countByHaVotadoTrue();

    /**
     * 🔹 Cuenta total de usuarios por rol (sin importar mayúsculas)
     */
    long countByRoleIgnoreCase(String role);

    /**
     * 🔹 Cuenta de usuarios por rol que ya votaron (ha_votado = true)
     */
    long countByRoleIgnoreCaseAndHaVotadoTrue(String role);
}
