package com.elecciones.sistema.setup;

import com.elecciones.sistema.SetupData;
import com.elecciones.sistema.model.Partido;
import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.PartidoRepository;
import com.elecciones.sistema.repo.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetupDataTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private PartidoRepository partidoRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SetupData setupData;

    @Test
    void creaAdministradorYPartidosInicialesCuandoNoExisten() {
        when(userRepo.findByUsername("admin")).thenReturn(null);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded");
        when(partidoRepo.count()).thenReturn(0L);

        setupData.run();

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepo).save(userCaptor.capture());
        UserAccount creado = userCaptor.getValue();

        assertThat(creado.getUsername()).isEqualTo("admin");
        assertThat(creado.getPassword()).isEqualTo("encoded");
        assertThat(creado.getRole()).isEqualTo("ADMIN");

        verify(partidoRepo, times(3)).save(any(Partido.class));
    }

    @Test
    void noDuplicaDatosSiYaExisten() {
        UserAccount existente = new UserAccount();
        when(userRepo.findByUsername("admin")).thenReturn(existente);
        when(partidoRepo.count()).thenReturn(5L);

        setupData.run();

        verify(userRepo, never()).save(any(UserAccount.class));
        verify(partidoRepo, never()).save(any(Partido.class));
    }
}
