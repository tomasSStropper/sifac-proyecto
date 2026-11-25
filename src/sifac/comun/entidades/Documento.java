package sifac.comun.entidades;

import java.time.LocalDateTime;
import java.util.List;
import sifac.comun.estructuras.ListaEnlazada;

public abstract class Documento implements Comparable<Documento> {
    private final int numero;
    private final Cliente cliente;
    private final double impuesto;
    private final LocalDateTime fecha;
    private final ListaEnlazada<LineaDetalle> detalles = new ListaEnlazada<>();
    private final ListaEnlazada<RegistroEstado> historial = new ListaEnlazada<>();

    public Documento(int numero, Cliente cliente, double impuesto) {
        if (numero <= 0 || cliente == null) {
            throw new IllegalArgumentException("Los datos del documento no son válidos.");
        }
        validarPorcentaje(impuesto);
        this.numero = numero;
        this.cliente = cliente;
        this.impuesto = impuesto;
        this.fecha = LocalDateTime.now();
        historial.agregar(new RegistroEstado(EstadoDocumento.ABIERTO));
    }

    public int getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<LineaDetalle> getDetalles() {
        return detalles.aLista();
    }

    public void agregarDetalle(LineaDetalle detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle es obligatorio.");
        }
        exigirEstado(EstadoDocumento.ABIERTO);
        detalles.agregar(detalle);
    }

    public boolean quitarDetalle(LineaDetalle detalle) {
        exigirEstado(EstadoDocumento.ABIERTO);
        return detalles.eliminar(detalle);
    }

    public boolean contieneDetalle(LineaDetalle detalle) {
        return detalles.contiene(detalle);
    }

    public int cantidadDetalles() {
        return detalles.cantidad();
    }

    public double calcularSubtotal() {
        double subtotal = 0;
        for (LineaDetalle detalle : detalles) {
            subtotal += detalle.calcularSubtotal();
        }
        return subtotal;
    }

    public synchronized EstadoDocumento getEstado() {
        return historial.getUltimo().getEstado();
    }

    public synchronized void cambiarEstado(EstadoDocumento nuevo) {
        EstadoDocumento actual = getEstado();
        if (!actual.puedePasarA(nuevo)) {
            throw new IllegalStateException("El documento " + numero + " no puede pasar de "
                    + actual.getDescripcion() + " a " + (nuevo == null ? "null" : nuevo.getDescripcion()) + ".");
        }
        historial.agregar(new RegistroEstado(nuevo));
    }

    public synchronized List<RegistroEstado> getHistorial() {
        return historial.aLista();
    }

    public int signoInventario() {
        return -1;
    }

    public double montoParaCaja() {
        return calcularTotal();
    }

    protected static void validarPorcentaje(double porcentaje) {
        if (!Double.isFinite(porcentaje) || porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    private void exigirEstado(EstadoDocumento esperado) {
        if (getEstado() != esperado) {
            throw new IllegalStateException("El documento " + numero + " ya no se puede modificar: está "
                    + getEstado().getDescripcion().toLowerCase() + ".");
        }
    }

    public abstract String getTipo();

    public abstract double calcularTotal();

    @Override
    public int compareTo(Documento otro) {
        return Integer.compare(numero, otro.numero);
    }

    @Override
    public String toString() {
        return getTipo() + " N.º " + numero;
    }
}
