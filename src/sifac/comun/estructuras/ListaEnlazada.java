package sifac.comun.estructuras;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ListaEnlazada<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> ultimo;
    private int cantidad;

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            ultimo.setSiguiente(nuevo);
        }
        ultimo = nuevo;
        cantidad++;
    }

    public boolean eliminar(T dato) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null && actual.getDato() != dato) {
            anterior = actual;
            actual = actual.getSiguiente();
        }
        if (actual == null) {
            return false;
        }
        if (anterior == null) {
            cabeza = actual.getSiguiente();
        } else {
            anterior.setSiguiente(actual.getSiguiente());
        }
        if (actual == ultimo) {
            ultimo = anterior;
        }
        cantidad--;
        return true;
    }

    public boolean contiene(T dato) {
        for (Nodo<T> n = cabeza; n != null; n = n.getSiguiente()) {
            if (n.getDato() == dato) {
                return true;
            }
        }
        return false;
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }

    public T getUltimo() {
        return ultimo == null ? null : ultimo.getDato();
    }

    public int cantidad() {
        return cantidad;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public List<T> aLista() {
        List<T> copia = new ArrayList<>();
        for (T dato : this) {
            copia.add(dato);
        }
        return copia;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.getDato();
                actual = actual.getSiguiente();
                return dato;
            }
        };
    }
}
