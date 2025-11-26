package com.elecciones.sistema.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulacionProgresoServiceTest {

    private SimulacionProgresoService service;

    @BeforeEach
    void setUp() {
        service = new SimulacionProgresoService();
    }

    @Test
    void testReset() {
        service.setTotal(50);
        service.setProcesados(20);

        service.reset(100);

        assertThat(service.getTotal()).isEqualTo(100);
        assertThat(service.getProcesados()).isEqualTo(0);
    }

    @Test
    void testAumentar_NoHaceNadaCuandoTotalEsCero() {
        service.reset(0);

        service.aumentar();

        assertThat(service.getProcesados()).isEqualTo(0);
    }

    @Test
    void testAumentar_IncrementaCuandoHayCapacidad() {
        service.reset(5); // total = 5

        service.aumentar(); // 1
        service.aumentar(); // 2
        service.aumentar(); // 3

        assertThat(service.getProcesados()).isEqualTo(3);
    }

    @Test
    void testAumentar_NoSobrepasaElLimite() {
        service.reset(3);

        service.aumentar(); // 1
        service.aumentar(); // 2
        service.aumentar(); // 3
        service.aumentar(); // NO incrementa
        service.aumentar(); // NO incrementa

        assertThat(service.getProcesados()).isEqualTo(3);
    }

    @Test
    void testAumentar_RespetaLimiteRedondeado() {
        service.reset(3); // Math.round(3) = 3

        for (int i = 0; i < 10; i++) {
            service.aumentar();
        }

        assertThat(service.getProcesados()).isEqualTo(3);
    }
}
