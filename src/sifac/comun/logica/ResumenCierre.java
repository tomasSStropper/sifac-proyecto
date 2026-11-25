package sifac.comun.logica;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import sifac.comun.entidades.Documento;

public class ResumenCierre {
    private final LocalDate fecha;
    private final Map<String, Integer> cantidadPorTipo = new LinkedHashMap<>();
    private final Map<String, Double> montoPorTipo = new LinkedHashMap<>();
    private int cantidadDocumentos;
    private double total;

    public ResumenCierre(LocalDate fecha) {
        this.fecha = fecha;
    }

    void sumar(Documento documento) {
        String tipo = documento.getTipo();
        cantidadPorTipo.merge(tipo, 1, Integer::sum);
        montoPorTipo.merge(tipo, documento.montoParaCaja(), Double::sum);
        cantidadDocumentos++;
        total += documento.montoParaCaja();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Map<String, Integer> getCantidadPorTipo() {
        return Collections.unmodifiableMap(cantidadPorTipo);
    }

    public Map<String, Double> getMontoPorTipo() {
        return Collections.unmodifiableMap(montoPorTipo);
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public double getTotal() {
        return total;
    }
}
