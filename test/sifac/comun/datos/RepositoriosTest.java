package sifac.comun.datos;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import sifac.comun.entidades.Cliente;
import sifac.padron.datos.RepositorioPadron;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriosTest {

    private RepositorioClientes desdePadron() throws IOException {
        RepositorioPadron padron = new RepositorioPadron();
        padron.cargar("PADRON_SINTETICO.txt", "Distelec.txt");
        return new RepositorioClientesPadron(padron);
    }

    @Test
    void consultaClientePorCedulaEnElPadron() throws IOException {
        RepositorioClientes clientes = desdePadron();

        Cliente cliente = clientes.buscar("104560789");

        assertNotNull(cliente);
        assertEquals("BRYAN ALEJANDRO VEGA RONDON", cliente.getNombre());
    }

    @Test
    void aceptaLaCedulaConGuiones() throws IOException {
        RepositorioClientes clientes = desdePadron();

        assertNotNull(clientes.buscar("1-0456-0789"));
    }

    @Test
    void cedulaInexistenteDevuelveNull() throws IOException {
        RepositorioClientes clientes = desdePadron();

        assertNull(clientes.buscar("999999999"));
    }

    @Test
    void repositorioEnMemoriaRechazaCedulaDuplicada() {
        RepositorioClientes clientes = new RepositorioClientesMemoria();
        clientes.guardar(new Cliente("1-1111-1111", "Rafael Mora"));

        assertThrows(IllegalArgumentException.class,
                () -> clientes.guardar(new Cliente("1-1111-1111", "Otro nombre")));
    }
}
