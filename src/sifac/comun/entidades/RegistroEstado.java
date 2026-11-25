package sifac.comun.entidades;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroEstado {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final EstadoDocumento estado;
    private final LocalDateTime momento;
    private final String hilo;

    public RegistroEstado(EstadoDocumento estado) {
        this.estado = estado;
        this.momento = LocalDateTime.now();
        this.hilo = Thread.currentThread().getName();
    }

    public EstadoDocumento getEstado() {
        return estado;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public String getHilo() {
        return hilo;
    }

    @Override
    public String toString() {
        return FORMATO.format(momento) + "  " + estado.getDescripcion() + "  [" + hilo + "]";
    }
}
