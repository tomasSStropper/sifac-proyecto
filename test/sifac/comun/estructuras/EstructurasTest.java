package sifac.comun.estructuras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstructurasTest {

    @Test
    void listaEliminaCabezaYUltimoSinPerderLaCadena() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        String a = "a";
        String b = "b";
        String c = "c";
        lista.agregar(a);
        lista.agregar(b);
        lista.agregar(c);

        assertTrue(lista.eliminar(a));
        assertTrue(lista.eliminar(c));
        lista.agregar("d");

        assertEquals(2, lista.cantidad());
        assertEquals("b", lista.obtener(0));
        assertEquals("d", lista.obtener(1));
        assertEquals("d", lista.getUltimo());
    }

    @Test
    void listaNoEliminaLoQueNoTiene() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregar("a");

        assertFalse(lista.eliminar("x"));
        assertEquals(1, lista.cantidad());
    }

    @Test
    void pilaDevuelveEnOrdenInverso() {
        Pila<Integer> pila = new Pila<>();
        pila.apilar(1);
        pila.apilar(2);
        pila.apilar(3);

        assertEquals(Integer.valueOf(3), pila.desapilar());
        assertEquals(Integer.valueOf(2), pila.desapilar());
        assertEquals(1, pila.cantidad());
    }

    @Test
    void pilaVaciaDevuelveNull() {
        Pila<Integer> pila = new Pila<>();

        assertNull(pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void colaRespetaElOrdenDeLlegada() {
        Cola<String> cola = new Cola<>();
        cola.encolar("FC-1");
        cola.encolar("FCR-2");
        cola.encolar("NC-3");

        assertEquals("FC-1", cola.desencolar());
        assertEquals("FCR-2", cola.desencolar());
        assertEquals("NC-3", cola.desencolar());
        assertNull(cola.desencolar());
    }

    @Test
    void colaFuncionaDespuesDeVaciarse() {
        Cola<String> cola = new Cola<>();
        cola.encolar("FC-100");
        cola.desencolar();

        cola.encolar("FC-200");

        assertEquals("FC-200", cola.verFrente());
        assertEquals(1, cola.cantidad());
    }
}
