package com.elecciones.sistema.config;

import com.elecciones.sistema.model.*;
import com.elecciones.sistema.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PartidoRepository partidoRepository;
    private final CandidatoRepository candidatoRepository;
    private final PerteneceRepository perteneceRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (candidatoRepository.count() == 0 && partidoRepository.count() == 0) {
            System.out.println("⚙️ Cargando datos iniciales de prueba...");

            // === Crear partidos ===
            Partido p1 = new Partido();
            p1.setNombre("Partido Verde");
            p1.setTipoLista("Abierta");
            p1.setCircunscripcion("Ordinario");
            partidoRepository.save(p1);

            Partido p2 = new Partido();
            p2.setNombre("Partido Azul");
            p2.setTipoLista("Cerrada");
            p2.setCircunscripcion("Ordinario");
            partidoRepository.save(p2);

            // === Crear candidatos ===
            Candidato c1 = new Candidato();
            c1.setCedula(1001L);
            c1.setNombre("Laura Gómez");
            c1.setCircunscripcion("Ordinario");
            candidatoRepository.save(c1);

            Candidato c2 = new Candidato();
            c2.setCedula(1002L);
            c2.setNombre("Carlos Ruiz");
            c2.setCircunscripcion("Ordinario");
            candidatoRepository.save(c2);

            // === Crear relaciones pertenece ===
            Pertenece rel1 = new Pertenece();
            rel1.setPartido(p1);          // 🔹 set PK explícitamente
            rel1.setCandidato(c1);       // 🔹 set PK explícitamente
            rel1.setOrdenCandidatos(1);
            // 🔹 nuevo nombre de campo
            //rel1.setPartido(p1);
            //rel1.setCandidato(c1);
            perteneceRepository.save(rel1);

            Pertenece rel2 = new Pertenece();
            rel2.setPartido(p2);
            rel2.setCandidato(c2);
            rel2.setOrdenCandidatos(1);
            //rel2.setPartido(p2);
            //rel2.setCandidato(c2);
            perteneceRepository.save(rel2);

            // === Crear votante de prueba ===
            if (userAccountRepository.findByUsername("1175500265") == null) {
                UserAccount votante = new UserAccount();
                votante.setUsername("1175500265");
                votante.setPassword(passwordEncoder.encode("votar123"));
                votante.setRole("VOTANTE");
                votante.setHaVotado(false);
                userAccountRepository.save(votante);
            }

            System.out.println("✅ Datos de prueba cargados correctamente.");
        } else {
            System.out.println("ℹ️ Datos ya existen, no se cargan duplicados.");
        }
    }
}
