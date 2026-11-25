package sifac.comun.logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.datos.RepositorioDocumentos;
import sifac.comun.datos.RepositorioProductos;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.RegistroEstado;
import sifac.comun.estructuras.Pila;

public class ServicioFacturacion {
    public static final double IMPUESTO = 13;
    public static final double DESCUENTO_CONTADO = 5;
    public static final double RECARGO_CREDITO = 2;
    public static final double DESCUENTO_NOTA = 5;
    public static final int DIAS_VIGENCIA_PROFORMA = 15;

    private final RepositorioClientes clientes;
    private final RepositorioProductos productos;
    private final RepositorioDocumentos documentos;
    private final ColaEnvio colaEnvio;
    private final Pila<LineaDetalle> deshacer = new Pila<>();
    private int consecutivo = 1;

    public ServicioFacturacion(RepositorioClientes clientes, RepositorioProductos productos,
                               RepositorioDocumentos documentos, ColaEnvio colaEnvio) {
        if (clientes == null || productos == null || documentos == null || colaEnvio == null) {
            throw new IllegalArgumentException("Los repositorios y la cola son obligatorios.");
        }
        this.clientes = clientes;
        this.productos = productos;
        this.documentos = documentos;
        this.colaEnvio = colaEnvio;
    }

    public void registrarCliente(String cedula, String nombre) {
        clientes.guardar(new Cliente(cedula, nombre));
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public Cliente consultarCliente(String cedula) {
        Cliente cliente = clientes.buscar(cedula);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente " + cedula + " no está registrado.");
        }
        return cliente;
    }

    public Documento crearDocumento(TipoDocumento tipo, String cedula) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de documento no es válido.");
        }
        Cliente cliente = consultarCliente(cedula);
        Documento documento = tipo.crear(siguienteNumero(), cliente);
        deshacer.vaciar();
        return documento;
    }

    public LineaDetalle agregarProducto(Documento documento, String codigo, int cantidad) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        Producto producto = productos.buscar(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("El producto " + codigo + " no existe.");
        }
        if (documento.getEstado() != EstadoDocumento.ABIERTO) {
            throw new IllegalStateException("El documento " + documento.getNumero() + " ya fue emitido.");
        }
        aplicarInventario(documento.signoInventario(), producto, cantidad);
        LineaDetalle linea = new LineaDetalle(producto, cantidad);
        documento.agregarDetalle(linea);
        deshacer.apilar(linea);
        return linea;
    }

    public boolean deshacerUltimaLinea(Documento documento) {
        LineaDetalle ultima = deshacer.verCima();
        if (ultima == null || documento == null || !documento.contieneDetalle(ultima)) {
            return false;
        }
        aplicarInventario(-documento.signoInventario(), ultima.getProducto(), ultima.getCantidad());
        documento.quitarDetalle(ultima);
        deshacer.desapilar();
        return true;
    }

    public void cancelar(Documento documento) {
        if (documento == null || documento.getEstado() != EstadoDocumento.ABIERTO) {
            return;
        }
        for (LineaDetalle linea : documento.getDetalles()) {
            aplicarInventario(-documento.signoInventario(), linea.getProducto(), linea.getCantidad());
            documento.quitarDetalle(linea);
        }
        deshacer.vaciar();
    }

    public void emitir(Documento documento) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (documento.cantidadDetalles() == 0) {
            throw new IllegalStateException("No se puede emitir un documento sin líneas.");
        }
        documento.cambiarEstado(EstadoDocumento.EMITIDO);
        documentos.guardar(documento);
        colaEnvio.encolar(documento);
        deshacer.vaciar();
    }

    public ResumenCierre cierreDeCaja(LocalDate fecha) {
        ResumenCierre resumen = new ResumenCierre(fecha);
        for (Documento documento : documentos.listar()) {
            if (documento.getFecha().toLocalDate().equals(fecha)) {
                resumen.sumar(documento);
            }
        }
        return resumen;
    }

    public ResumenCierre cierreDeCaja() {
        return cierreDeCaja(LocalDate.now());
    }

    public List<Documento> listarDelDiaPorMonto(boolean descendente) {
        LocalDate hoy = LocalDate.now();
        List<Documento> delDia = new ArrayList<>();
        for (Documento documento : documentos.listarPorMonto(descendente)) {
            if (documento.getFecha().toLocalDate().equals(hoy)) {
                delDia.add(documento);
            }
        }
        return delDia;
    }

    public Documento buscarPorNumero(int numero) {
        return documentos.buscarPorNumero(numero);
    }

    public List<RegistroEstado> historialDe(int numero) {
        Documento documento = buscarPorNumero(numero);
        if (documento == null) {
            throw new IllegalArgumentException("No existe el documento N.º " + numero + ".");
        }
        return documento.getHistorial();
    }

    public List<Producto> listarProductos() {
        return productos.listar();
    }

    public int pendientesDeEnvio() {
        return colaEnvio.pendientes();
    }

    private synchronized int siguienteNumero() {
        return consecutivo++;
    }

    private static void aplicarInventario(int signo, Producto producto, int cantidad) {
        if (signo < 0) {
            producto.descontar(cantidad);
        } else if (signo > 0) {
            producto.reponer(cantidad);
        }
    }
}
