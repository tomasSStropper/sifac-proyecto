package sifac.comun.logica;

import sifac.comun.entidades.Documento;
import sifac.comun.estructuras.Cola;

public class ColaEnvio {
    private final Cola<Documento> cola = new Cola<>();
    private boolean cerrada;

    public synchronized void encolar(Documento documento) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (cerrada) {
            throw new IllegalStateException("La cola de envío está cerrada.");
        }
        cola.encolar(documento);
        notifyAll();
    }

    public synchronized Documento tomar() throws InterruptedException {
        while (cola.estaVacia() && !cerrada) {
            wait();
        }
        return cola.desencolar();
    }

    public synchronized void cerrar() {
        cerrada = true;
        notifyAll();
    }

    public synchronized int pendientes() {
        return cola.cantidad();
    }
}
