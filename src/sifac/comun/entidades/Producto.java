package sifac.comun.entidades;

public class Producto {
    private final String codigo;
    private final String nombre;
    private final double precio;
    private int existencias;

    public Producto(String codigo, String nombre, double precio, int existencias) {
        if (codigo == null || codigo.trim().isEmpty()
                || nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El código y el nombre son obligatorios.");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.precio = precio;
        this.existencias = existencias;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public synchronized int getExistencias() {
        return existencias;
    }

    public synchronized void descontar(int cantidad) {
        validarCantidad(cantidad);
        if (cantidad > existencias) {
            throw new ExistenciasInsuficientesException(codigo, existencias, cantidad);
        }
        existencias -= cantidad;
    }

    public synchronized void reponer(int cantidad) {
        validarCantidad(cantidad);
        existencias += cantidad;
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
