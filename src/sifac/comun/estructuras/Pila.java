package sifac.comun.estructuras;

public class Pila<T> {
    private Nodo<T> cima;
    private int cantidad;

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(cima);
        cima = nuevo;
        cantidad++;
    }

    public T desapilar() {
        if (cima == null) {
            return null;
        }
        T dato = cima.getDato();
        cima = cima.getSiguiente();
        cantidad--;
        return dato;
    }

    public T verCima() {
        return cima == null ? null : cima.getDato();
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int cantidad() {
        return cantidad;
    }

    public void vaciar() {
        cima = null;
        cantidad = 0;
    }
}
