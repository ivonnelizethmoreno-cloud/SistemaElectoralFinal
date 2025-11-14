package com.elecciones.sistema;

import com.elecciones.sistema.model.Partido;
import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.PartidoRepository;
import com.elecciones.sistema.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetupData implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final PartidoRepository partidoRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ================================
        // 🔹 CREAR USUARIO ADMIN POR DEFECTO
        // ================================
        if (userRepo.findByUsername("admin") == null) {
            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setHaVotado(false);
            admin.setNombreUsuario("Administrador General");
            admin.setCorreoElectronico("admin@sistema.com");
            userRepo.save(admin);

            System.out.println("✅ Usuario admin creado: admin / admin123");
        }

        // ================================
        // 🔹 CARGAR PARTIDOS DE PRUEBA SOLO SI LA TABLA ESTÁ VACÍA
        // ================================
        if (partidoRepo.count() == 0) {
            partidoRepo.save(Partido.builder()
                    //.partidoId("PX")
                    .nombre("Partido de Integración Nacional")
                    .tipoLista("Abierta")
                    .circunscripcion("Ordinario")
                    .build());

            partidoRepo.save(Partido.builder()
                    //.partidoId("PY")
                    .nombre("Movimiento Popular Unido")
                    .tipoLista("Cerrada")
                    .circunscripcion("Ordinario")
                    .build());

            partidoRepo.save(Partido.builder()
                  //.partidoId("PZ")
                    .nombre("Coalición del Pueblo")
                    .tipoLista("Abierta")
                    .circunscripcion("Indigena")
                    .build());

            System.out.println("✅ Partidos iniciales cargados en base de datos (PX, PY, PZ)");
        }

        // 🟢 Resto de datos (votantes, candidatos) se cargan desde CSV
        System.out.println("🟡 Sistema listo. Cargue votantes y candidatos desde el panel administrador.");
    }
}
