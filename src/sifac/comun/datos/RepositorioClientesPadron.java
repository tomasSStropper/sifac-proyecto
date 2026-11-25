package sifac.comun.datos;

import java.util.List;
import sifac.comun.entidades.Cliente;
import sifac.padron.datos.RepositorioPadron;
import sifac.padron.entidades.Elector;

public class RepositorioClientesPadron implements RepositorioClientes {
    private final RepositorioPadron padron;
    private final RepositorioClientes registrados = new RepositorioClientesMemoria();

    public RepositorioClientesPadron(RepositorioPadron padron) {
        if (padron == null) {
            throw new IllegalArgumentException("El padrón es obligatorio.");
        }
        this.padron = padron;
    }

    @Override
    public void guardar(Cliente cliente) {
        if (cliente != null && padron.buscarBinaria(normalizar(cliente.getCedula())) != null) {
            throw new IllegalArgumentException("La cédula ya existe en el padrón; consúltela en lugar de registrarla.");
        }
        registrados.guardar(cliente);
    }

    @Override
    public Cliente buscar(String cedula) {
        if (cedula == null) {
            return null;
        }
        String normalizada = normalizar(cedula);
        Cliente cliente = registrados.buscar(normalizada);
        if (cliente != null) {
            return cliente;
        }
        Elector elector = padron.buscarBinaria(normalizada);
        if (elector == null) {
            return null;
        }
        Cliente desdePadron = new Cliente(elector.getCedula(), elector.getNombreCompleto());
        registrados.guardar(desdePadron);
        return desdePadron;
    }

    @Override
    public List<Cliente> listar() {
        return registrados.listar();
    }

    private static String normalizar(String cedula) {
        return cedula.replace("-", "").replace(" ", "").trim();
    }
}
