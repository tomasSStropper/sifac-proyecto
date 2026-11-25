package sifac.comun.logica;

import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.FacturaContado;
import sifac.comun.entidades.FacturaCredito;
import sifac.comun.entidades.NotaCredito;
import sifac.comun.entidades.Proforma;

public enum TipoDocumento {
    FACTURA_CONTADO("Factura de contado (descuento " + ServicioFacturacion.DESCUENTO_CONTADO + " %)") {
        @Override
        Documento crear(int numero, Cliente cliente) {
            return new FacturaContado(numero, cliente, ServicioFacturacion.IMPUESTO,
                    ServicioFacturacion.DESCUENTO_CONTADO);
        }
    },
    FACTURA_CREDITO("Factura de crédito (recargo " + ServicioFacturacion.RECARGO_CREDITO + " %)") {
        @Override
        Documento crear(int numero, Cliente cliente) {
            return new FacturaCredito(numero, cliente, ServicioFacturacion.IMPUESTO,
                    ServicioFacturacion.RECARGO_CREDITO);
        }
    },
    NOTA_CREDITO("Nota de crédito (devolución de contado)") {
        @Override
        Documento crear(int numero, Cliente cliente) {
            return new NotaCredito(numero, cliente, ServicioFacturacion.IMPUESTO,
                    ServicioFacturacion.DESCUENTO_NOTA);
        }
    },
    PROFORMA("Proforma (vigencia " + ServicioFacturacion.DIAS_VIGENCIA_PROFORMA + " días)") {
        @Override
        Documento crear(int numero, Cliente cliente) {
            return new Proforma(numero, cliente, ServicioFacturacion.IMPUESTO,
                    ServicioFacturacion.DIAS_VIGENCIA_PROFORMA);
        }
    };

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    abstract Documento crear(int numero, Cliente cliente);
}
