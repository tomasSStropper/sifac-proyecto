package sifac.comun.logica;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sifac.comun.datos.RepositorioClientesMemoria;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioProductosMemoria;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;
import sifac.comun.entidades.ExistenciasInsuficientesException;
import sifac.comun.entidades.FacturaContado;
import sifac.comun.entidades.FacturaCredito;
import sifac.comun.entidades.NotaCredito;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.Proforma;

import static org.junit.jupiter.api.Assertions.*;

class ServicioFacturacionTest {

    private static final String CEDULA = "1-1111-1111";

    private RepositorioProductosMemoria productos;
    private ColaEnvio cola;
    private ServicioFacturacion servicio;

    @BeforeEach
    void preparar() {
        productos = new RepositorioProductosMemoria();
        cola = new ColaEnvio();
        servicio = new ServicioFacturacion(new RepositorioClientesMemoria(), productos,
                new RepositorioDocumentosMemoria(), cola);
        servicio.registrarCliente(CEDULA, "Rafael Mora");
    }

    private Documento emitirConTubos(TipoDocumento tipo, int cantidad) {
        Documento documento = servicio.crearDocumento(tipo, CEDULA);
        servicio.agregarProducto(documento, "PVC-050", cantidad);
        servicio.emitir(documento);
        return documento;
    }

    private int existencias(String codigo) {
        return productos.buscar(codigo).getExistencias();
    }

    @Test
    void emiteLosCuatroTiposDeDocumento() {
        assertTrue(servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA) instanceof FacturaContado);
        assertTrue(servicio.crearDocumento(TipoDocumento.FACTURA_CREDITO, CEDULA) instanceof FacturaCredito);
        assertTrue(servicio.crearDocumento(TipoDocumento.NOTA_CREDITO, CEDULA) instanceof NotaCredito);
        assertTrue(servicio.crearDocumento(TipoDocumento.PROFORMA, CEDULA) instanceof Proforma);
    }

    @Test
    void tipoDeDocumentoInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crearDocumento(null, CEDULA));
    }

    @Test
    void clienteNoRegistradoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, "9-9999-9999"));
    }

    @Test
    void agregarLineaDescuentaInventario() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        servicio.agregarProducto(factura, "CEM-50", 3);

        assertEquals(1, factura.cantidadDetalles());
        assertEquals(37, existencias("CEM-50"));
    }

    @Test
    void productoInexistenteSeRechaza() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        assertThrows(IllegalArgumentException.class, () -> servicio.agregarProducto(factura, "NO-EXISTE", 1));
        assertEquals(0, factura.cantidadDetalles());
    }

    @Test
    void cantidadCeroSeRechaza() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        assertThrows(IllegalArgumentException.class, () -> servicio.agregarProducto(factura, "PVC-050", 0));
        assertEquals(100, existencias("PVC-050"));
    }

    @Test
    void existenciasInsuficientesSeRechazanSinTocarElInventario() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        assertThrows(ExistenciasInsuficientesException.class,
                () -> servicio.agregarProducto(factura, "MAR-16", 3));
        assertEquals(2, existencias("MAR-16"));
        assertEquals(0, factura.cantidadDetalles());
    }

    @Test
    void proformaNoDescuentaInventario() {
        Documento proforma = servicio.crearDocumento(TipoDocumento.PROFORMA, CEDULA);

        servicio.agregarProducto(proforma, "PVC-050", 10);

        assertEquals(100, existencias("PVC-050"));
    }

    @Test
    void notaDeCreditoDevuelveInventario() {
        Documento nota = servicio.crearDocumento(TipoDocumento.NOTA_CREDITO, CEDULA);

        servicio.agregarProducto(nota, "PVC-050", 4);

        assertEquals(104, existencias("PVC-050"));
    }

    @Test
    void deshacerQuitaLaUltimaLineaYDevuelveElInventario() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);
        servicio.agregarProducto(factura, "PVC-050", 10);
        servicio.agregarProducto(factura, "CLA-2", 5);
        double subtotalAntes = factura.calcularSubtotal();

        boolean deshizo = servicio.deshacerUltimaLinea(factura);

        assertTrue(deshizo);
        assertEquals(subtotalAntes - 6000, factura.calcularSubtotal(), 0.01);
        assertEquals(1, factura.cantidadDetalles());
        assertEquals("PVC-050", factura.getDetalles().get(0).getProducto().getCodigo());
        assertEquals(200, existencias("CLA-2"));
    }

    @Test
    void deshacerSinLineasDevuelveFalse() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        assertFalse(servicio.deshacerUltimaLinea(factura));
        assertEquals(0, factura.cantidadDetalles());
    }

    @Test
    void cancelarDevuelveTodoElInventario() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);
        servicio.agregarProducto(factura, "PVC-050", 10);
        servicio.agregarProducto(factura, "CEM-50", 2);

        servicio.cancelar(factura);

        assertEquals(100, existencias("PVC-050"));
        assertEquals(40, existencias("CEM-50"));
    }

    @Test
    void emitirSinLineasSeRechaza() {
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);

        assertThrows(IllegalStateException.class, () -> servicio.emitir(factura));
        assertEquals(EstadoDocumento.ABIERTO, factura.getEstado());
    }

    @Test
    void emitirCambiaElEstadoYEncola() {
        Documento factura = emitirConTubos(TipoDocumento.FACTURA_CONTADO, 10);

        assertEquals(EstadoDocumento.EMITIDO, factura.getEstado());
        assertEquals(2, factura.getHistorial().size());
        assertEquals(1, servicio.pendientesDeEnvio());
    }

    @Test
    void documentoEmitidoNoAceptaMasLineas() {
        Documento factura = emitirConTubos(TipoDocumento.FACTURA_CONTADO, 10);

        assertThrows(IllegalStateException.class, () -> servicio.agregarProducto(factura, "PVC-050", 1));
        assertEquals(90, existencias("PVC-050"));
    }

    @Test
    void cierreDeCajaSumaElDiaYNoCuentaDineroDeProformas() {
        emitirConTubos(TipoDocumento.FACTURA_CONTADO, 10);
        emitirConTubos(TipoDocumento.FACTURA_CREDITO, 10);
        emitirConTubos(TipoDocumento.NOTA_CREDITO, 10);
        emitirConTubos(TipoDocumento.PROFORMA, 10);

        ResumenCierre cierre = servicio.cierreDeCaja(LocalDate.now());

        assertEquals(4, cierre.getCantidadDocumentos());
        assertEquals(10735 + 11526 - 10735, cierre.getTotal(), 0.01);
    }

    @Test
    void listarPorMontoOrdenaDeMayorAMenor() {
        Documento pequena = emitirConTubos(TipoDocumento.FACTURA_CONTADO, 1);
        Documento grande = emitirConTubos(TipoDocumento.FACTURA_CONTADO, 20);
        Documento mediana = emitirConTubos(TipoDocumento.FACTURA_CONTADO, 5);

        List<Documento> lista = servicio.listarDelDiaPorMonto(true);

        assertSame(grande, lista.get(0));
        assertSame(mediana, lista.get(1));
        assertSame(pequena, lista.get(2));
    }

    @Test
    void buscarPorNumeroEncuentraElEmitidoYNoInventaOtros() {
        emitirConTubos(TipoDocumento.FACTURA_CONTADO, 1);
        Documento segundo = emitirConTubos(TipoDocumento.FACTURA_CREDITO, 1);
        emitirConTubos(TipoDocumento.FACTURA_CONTADO, 1);

        assertSame(segundo, servicio.buscarPorNumero(segundo.getNumero()));
        assertNull(servicio.buscarPorNumero(999));
    }

    @Test
    void consecutivoEsCorrelativo() {
        Documento primero = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);
        Documento segundo = servicio.crearDocumento(TipoDocumento.PROFORMA, CEDULA);

        assertEquals(primero.getNumero() + 1, segundo.getNumero());
    }

    @Test
    void elInventarioNoSeVuelveNegativo() {
        Producto martillo = productos.buscar("MAR-16");
        Documento factura = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, CEDULA);
        servicio.agregarProducto(factura, "MAR-16", 2);

        assertThrows(ExistenciasInsuficientesException.class, () -> servicio.agregarProducto(factura, "MAR-16", 1));
        assertEquals(0, martillo.getExistencias());
    }
}
