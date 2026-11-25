package sifac.comun.entidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentosTest {

    private final Cliente cliente = new Cliente("104560789", "BRYAN ALEJANDRO VEGA RONDON");
    private final Producto tubo = new Producto("PVC-050", "Tubo PVC 1/2", 1000, 100);

    private Documento conDiezTubos(Documento documento) {
        documento.agregarDetalle(new LineaDetalle(tubo, 10));
        return documento;
    }

    @Test
    void facturaContadoAplicaDescuentoEImpuesto() {
        Documento factura = conDiezTubos(new FacturaContado(1, cliente, 13, 5));

        assertEquals(10735.00, factura.calcularTotal(), 0.01);
    }

    @Test
    void facturaCreditoAplicaImpuestoYRecargo() {
        Documento factura = conDiezTubos(new FacturaCredito(2, cliente, 13, 2));

        assertEquals(11526.00, factura.calcularTotal(), 0.01);
    }

    @Test
    void notaCreditoDevuelveElMontoEnNegativo() {
        Documento nota = conDiezTubos(new NotaCredito(3, cliente, 13, 5));

        assertEquals(-10735.00, nota.calcularTotal(), 0.01);
    }

    @Test
    void proformaSoloAplicaImpuestoYNoSumaEnCaja() {
        Documento proforma = conDiezTubos(new Proforma(4, cliente, 13, 15));

        assertEquals(11300.00, proforma.calcularTotal(), 0.01);
        assertEquals(0.00, proforma.montoParaCaja(), 0.01);
    }

    @Test
    void documentoSinLineasTotalizaCero() {
        Documento factura = new FacturaContado(5, cliente, 13, 5);

        assertEquals(0.00, factura.calcularTotal(), 0.01);
    }

    @Test
    void porcentajeFueraDeRangoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new FacturaContado(6, cliente, 13, 150));
    }

    @Test
    void lineaConCantidadCeroSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new LineaDetalle(tubo, 0));
    }

    @Test
    void elHistorialEmpiezaAbiertoYAvanzaEnOrden() {
        Documento factura = conDiezTubos(new FacturaContado(7, cliente, 13, 5));

        factura.cambiarEstado(EstadoDocumento.EMITIDO);
        factura.cambiarEstado(EstadoDocumento.PROCESADO);

        assertEquals(3, factura.getHistorial().size());
        assertEquals(EstadoDocumento.ABIERTO, factura.getHistorial().get(0).getEstado());
        assertEquals(EstadoDocumento.PROCESADO, factura.getEstado());
    }

    @Test
    void saltarseUnEstadoSeRechaza() {
        Documento factura = new FacturaContado(8, cliente, 13, 5);

        assertThrows(IllegalStateException.class, () -> factura.cambiarEstado(EstadoDocumento.PROCESADO));
        assertEquals(EstadoDocumento.ABIERTO, factura.getEstado());
    }

    @Test
    void productoRechazaVenderMasDeLoQueHay() {
        Producto martillo = new Producto("MAR-16", "Martillo 16 oz", 6500, 2);

        assertThrows(ExistenciasInsuficientesException.class, () -> martillo.descontar(3));
        assertEquals(2, martillo.getExistencias());
    }
}
