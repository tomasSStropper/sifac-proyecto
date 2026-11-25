package sifac.caja.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.datos.RepositorioClientesMemoria;
import sifac.comun.datos.RepositorioClientesPadron;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioProductosMemoria;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.RegistroEstado;
import sifac.comun.logica.ColaEnvio;
import sifac.comun.logica.ProcesadorEnvios;
import sifac.comun.logica.ResumenCierre;
import sifac.comun.logica.ServicioFacturacion;
import sifac.comun.logica.TipoDocumento;
import sifac.padron.datos.RepositorioPadron;

public class Principal {
    private static final String PADRON = "PADRON_SINTETICO.txt";
    private static final String DISTELEC = "Distelec.txt";
    private static final long DEMORA_ENVIO_MS = 3000;

    private final Scanner entrada = new Scanner(System.in);
    private final ServicioFacturacion servicio;

    public Principal(ServicioFacturacion servicio) {
        this.servicio = servicio;
    }

    public static void main(String[] args) throws InterruptedException {
        String rutaPadron = args.length > 0 ? args[0] : PADRON;
        String rutaDistelec = args.length > 1 ? args[1] : DISTELEC;

        ColaEnvio cola = new ColaEnvio();
        ServicioFacturacion servicio = new ServicioFacturacion(
                crearRepositorioClientes(rutaPadron, rutaDistelec),
                new RepositorioProductosMemoria(),
                new RepositorioDocumentosMemoria(),
                cola);

        Thread procesador = new Thread(new ProcesadorEnvios(cola, DEMORA_ENVIO_MS), "procesador-envios");
        procesador.start();

        new Principal(servicio).menu();

        int pendientes = servicio.pendientesDeEnvio();
        if (pendientes > 0) {
            System.out.println("Procesando " + pendientes + " documento(s) pendiente(s) antes de salir...");
        }
        cola.cerrar();
        procesador.join();
        System.out.println("Hasta luego.");
    }

    private static RepositorioClientes crearRepositorioClientes(String rutaPadron, String rutaDistelec) {
        if (new File(rutaPadron).exists() && new File(rutaDistelec).exists()) {
            try {
                RepositorioPadron padron = new RepositorioPadron();
                padron.cargar(rutaPadron, rutaDistelec);
                System.out.println("Clientes: padrón " + rutaPadron + " (" + padron.cantidad() + " electores).");
                return new RepositorioClientesPadron(padron);
            } catch (IOException e) {
                System.out.println("No se pudo leer el padrón: " + e.getMessage());
            }
        }
        System.out.println("Clientes: registro en memoria (sin padrón).");
        return new RepositorioClientesMemoria();
    }

    private void menu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\nSIFAC  (pendientes de envío: " + servicio.pendientesDeEnvio() + ")");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Consultar cliente por cédula");
            System.out.println("3. Ver clientes");
            System.out.println("4. Nuevo documento");
            System.out.println("5. Listar documentos del día por monto");
            System.out.println("6. Buscar documento por número");
            System.out.println("7. Cierre de caja del día");
            System.out.println("8. Ver inventario");
            System.out.println("0. Salir");
            try {
                opcion = leerNumero("Opción: ");
                switch (opcion) {
                    case 1:
                        registrarCliente();
                        break;
                    case 2:
                        consultarCliente();
                        break;
                    case 3:
                        verClientes();
                        break;
                    case 4:
                        nuevoDocumento();
                        break;
                    case 5:
                        listarPorMonto();
                        break;
                    case 6:
                        buscarDocumento();
                        break;
                    case 7:
                        cierreDeCaja();
                        break;
                    case 8:
                        verInventario();
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción incorrecta.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void registrarCliente() {
        String cedula = leerTexto("Cédula: ");
        String nombre = leerTexto("Nombre: ");
        servicio.registrarCliente(cedula, nombre);
        System.out.println("Cliente registrado.");
    }

    private void consultarCliente() {
        Cliente cliente = servicio.consultarCliente(leerTexto("Cédula: "));
        System.out.println(cliente);
    }

    private void verClientes() {
        List<Cliente> clientes = servicio.listarClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados ni consultados.");
        }
        for (Cliente cliente : clientes) {
            System.out.println(cliente);
        }
    }

    private void nuevoDocumento() {
        TipoDocumento[] tipos = TipoDocumento.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + ". " + tipos[i].getDescripcion());
        }
        int indiceTipo = leerNumero("Tipo: ");
        if (indiceTipo < 1 || indiceTipo > tipos.length) {
            System.out.println("El tipo de documento no es válido.");
            return;
        }
        String cedula = leerTexto("Cédula del cliente: ");
        Documento documento = servicio.crearDocumento(tipos[indiceTipo - 1], cedula);
        System.out.println(documento + " para " + documento.getCliente());

        while (true) {
            List<Producto> productos = servicio.listarProductos();
            mostrarProductos(productos);
            String opcion = leerTexto("Producto (número), D deshacer, E emitir, C cancelar: ").toUpperCase();
            try {
                if (opcion.equals("E")) {
                    servicio.emitir(documento);
                    mostrarDocumento(documento);
                    System.out.println("Emitido y encolado para envío.");
                    return;
                } else if (opcion.equals("C")) {
                    servicio.cancelar(documento);
                    System.out.println("Documento cancelado; el inventario quedó como estaba.");
                    return;
                } else if (opcion.equals("D")) {
                    System.out.println(servicio.deshacerUltimaLinea(documento)
                            ? "Se deshizo la última línea." : "No hay líneas para deshacer.");
                } else {
                    int indice = Integer.parseInt(opcion);
                    if (indice < 1 || indice > productos.size()) {
                        System.out.println("Producto incorrecto.");
                        continue;
                    }
                    int cantidad = leerNumero("Cantidad: ");
                    servicio.agregarProducto(documento, productos.get(indice - 1).getCodigo(), cantidad);
                    System.out.println("Producto agregado.");
                }
                mostrarLineas(documento);
            } catch (NumberFormatException e) {
                System.out.println("Opción incorrecta.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void listarPorMonto() {
        List<Documento> documentos = servicio.listarDelDiaPorMonto(true);
        if (documentos.isEmpty()) {
            System.out.println("No hay documentos emitidos.");
        }
        for (Documento documento : documentos) {
            System.out.printf("N.º %-4d %-20s %-35s CRC %12.2f  %s%n", documento.getNumero(), documento.getTipo(),
                    documento.getCliente().getNombre(), documento.calcularTotal(),
                    documento.getEstado().getDescripcion());
        }
    }

    private void buscarDocumento() {
        int numero = leerNumero("Número: ");
        Documento documento = servicio.buscarPorNumero(numero);
        if (documento == null) {
            System.out.println("No existe el documento N.º " + numero + ".");
            return;
        }
        mostrarDocumento(documento);
        System.out.println("Historial de estados:");
        for (RegistroEstado registro : documento.getHistorial()) {
            System.out.println("  " + registro);
        }
    }

    private void cierreDeCaja() {
        ResumenCierre resumen = servicio.cierreDeCaja();
        System.out.println("Cierre de caja del " + resumen.getFecha());
        for (Map.Entry<String, Integer> tipo : resumen.getCantidadPorTipo().entrySet()) {
            System.out.printf("  %-20s %3d documento(s)   CRC %12.2f%n", tipo.getKey(), tipo.getValue(),
                    resumen.getMontoPorTipo().get(tipo.getKey()));
        }
        System.out.printf("  Total del día (%d documentos): CRC %.2f%n",
                resumen.getCantidadDocumentos(), resumen.getTotal());
        System.out.println("  Las proformas se cuentan pero no suman: no mueven dinero.");
    }

    private void verInventario() {
        for (Producto producto : servicio.listarProductos()) {
            System.out.printf("%-8s %-28s CRC %10.2f   existencias %d%n", producto.getCodigo(),
                    producto.getNombre(), producto.getPrecio(), producto.getExistencias());
        }
    }

    private void mostrarProductos(List<Producto> productos) {
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            System.out.printf("%d. %-8s %-28s CRC %10.2f  (existencias %d)%n", i + 1, producto.getCodigo(),
                    producto.getNombre(), producto.getPrecio(), producto.getExistencias());
        }
    }

    private void mostrarLineas(Documento documento) {
        for (LineaDetalle detalle : documento.getDetalles()) {
            System.out.printf("   %s x %d = CRC %.2f%n", detalle.getProducto().getNombre(),
                    detalle.getCantidad(), detalle.calcularSubtotal());
        }
        System.out.printf("   Subtotal: CRC %.2f   Total: CRC %.2f%n",
                documento.calcularSubtotal(), documento.calcularTotal());
    }

    private void mostrarDocumento(Documento documento) {
        System.out.println("\n" + documento + "   [" + documento.getEstado().getDescripcion() + "]");
        System.out.println("Cliente: " + documento.getCliente());
        mostrarLineas(documento);
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) {
            return "0";
        }
        return entrada.nextLine().trim();
    }

    private int leerNumero(String mensaje) {
        try {
            return Integer.parseInt(leerTexto(mensaje));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingrese un número entero.");
        }
    }
}
