package com.elecciones.sistema.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class SimulacionProgresoService {

    @Getter @Setter
    private int total = 0;

    @Getter @Setter
    private int procesados = 0;

    public void reset(int total) {
        this.total = total;
        this.procesados = 0;
    }

    /*public void aumentar() {
        this.procesados++;
    }*/
    public void aumentar() {
        if (total == 0) return;

        int limite = (int) Math.round(total);

        if (procesados < limite) {
            this.procesados++;
        }
    }

}
