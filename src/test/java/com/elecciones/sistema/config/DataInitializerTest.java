 package com.elecciones.sistema.config;

import com.elecciones.sistema.model.*;
import com.elecciones.sistema.repo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private CandidatoRepository candidatoRepository;

    @Mock
    private PerteneceRepository perteneceRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer initializer;

    @Test
    void cargaDatosDePruebaCuandoNoHayRegistros() {
        when(candidatoRepository.count()).thenReturn(0L);
        when(partidoRepository.count()).thenReturn(0L);
        when(userAccountRepository.findByUsername("1175500265")).thenReturn(null);
        when(passwordEncoder.encode("votar123")).thenReturn("secure");

        initializer.run();

        verify(partidoRepository, times(2)).save(any(Partido.class));
        verify(candidatoRepository, times(2)).save(any(Candidato.class));
        verify(perteneceRepository, times(2)).save(any(Pertenece.class));
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void noRealizaCambiosSiYaHayDatos() {
        when(candidatoRepository.count()).thenReturn(3L);

        initializer.run();

        verifyNoInteractions(perteneceRepository);
        verify(userAccountRepository, never()).save(any(UserAccount.class));
        verify(partidoRepository, never()).save(any(Partido.class));
        verify(candidatoRepository, never()).save(any(Candidato.class));
    }
}
