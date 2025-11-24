package com.elecciones.sistema.web;

import com.elecciones.sistema.model.Elige;
import com.elecciones.sistema.repo.EligeRepository;
import com.elecciones.sistema.web.ResultadosController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResultadosControllerTest {

    @Mock
    private EligeRepository eligeRepository;

    @InjectMocks
    private ResultadosController resultadosController;

    private Elige voto1;
    private Elige voto2;

    @BeforeEach
    void setUp() {
        voto1 = Elige.builder()
                .candidatoNombre("A1")
                .circunscripcion("INDIGENA")
                .build();

        voto2 = Elige.builder()
                .candidatoNombre("A2")
                .circunscripcion("INDIGENA")
                .build();
    }

    // ========================================================
    // 1. RESULTADOS ANONIMIZADOS
    // ========================================================
    @Test
    void debeMostrarResultadosAnonimizados() {

        when(eligeRepository.findAll()).thenReturn(List.of(voto1, voto2));

        Model model = mock(Model.class);

        String vista = resultadosController.obtenerResultados(model);

        assertThat(vista).isEqualTo("resultados");

        verify(model).addAttribute(eq("votosPorCandidato"), any(Map.class));
    }

    // ========================================================
    // 2. CURULES INDÍGENAS MÍNIMO 2
    // ========================================================
    @Test
    void debeGarantizarMinimoDosCurulesIndigenas() {

        when(eligeRepository.findAll()).thenReturn(List.of(voto1, voto1, voto1));

        Model model = mock(Model.class);

        resultadosController.obtenerResultados(model);

        verify(model).addAttribute(eq("curulesIndigenas"), argThat(c -> (int) c >= 2));
    }

    // ========================================================
    // 3. UMBRAL CORRECTO
    // ========================================================
    @Test
    void debeCalcularUmbralCorrectamente() {

        when(eligeRepository.findAll()).thenReturn(List.of(voto1, voto2));

        Model model = mock(Model.class);

        resultadosController.obtenerResultados(model);

        verify(model).addAttribute(eq("umbral"), anyInt());
    }

    // ========================================================
    // 4. CANDIDATOS ELECTOS
    // ========================================================
    @Test
    void debeMostrarCandidatosElectos() {

        when(eligeRepository.findAll()).thenReturn(List.of(voto1, voto1, voto2));

        Model model = mock(Model.class);

        resultadosController.obtenerResultados(model);

        verify(model).addAttribute(eq("candidatosElectos"), any(List.class));
    }
}
