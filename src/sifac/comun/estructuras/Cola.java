package sifac.comun.estructuras;

public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> fin;
    private int cantidad;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        cantidad++;
    }

    public T desencolar() {
        if (frente == null) {
            return null;
        }
        Nodo<T> retirado = frente;
        frente = retirado.getSiguiente();
        if (frente == null) {
            fin = null;
        }
        retirado.setSiguiente(null);
        cantidad--;
        return retirado.getDato();
    }

    public T verFrente() {
        return frente == null ? null : frente.getDato();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int cantidad() {
        return cantidad;
    }
}
