package sifac.comun.logica;

import java.util.ArrayList;
import java.util.List;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;

public class ProcesadorEnvios implements Runnable {
    private final ColaEnvio cola;
    private final long demoraMilisegundos;
    private final List<Integer> procesados = new ArrayList<>();

    public ProcesadorEnvios(ColaEnvio cola, long demoraMilisegundos) {
        if (cola == null || demoraMilisegundos < 0) {
            throw new IllegalArgumentException("La cola es obligatoria y la demora no puede ser negativa.");
        }
        this.cola = cola;
        this.demoraMilisegundos = demoraMilisegundos;
    }

    @Override
    public void run() {
        try {
            Documento documento;
            while ((documento = cola.tomar()) != null) {
                if (demoraMilisegundos > 0) {
                    Thread.sleep(demoraMilisegundos);
                }
                documento.cambiarEstado(EstadoDocumento.PROCESADO);
                registrar(documento.getNumero());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void registrar(int numero) {
        procesados.add(numero);
    }

    public synchronized List<Integer> getProcesados() {
        return new ArrayList<>(procesados);
    }
}
