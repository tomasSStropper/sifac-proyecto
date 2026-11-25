package sifac.comun.entidades;

public class Proforma extends Documento {
    private final int diasVigencia;

    public Proforma(int numero, Cliente cliente, double impuesto, int diasVigencia) {
        super(numero, cliente, impuesto);
        if (diasVigencia <= 0) {
            throw new IllegalArgumentException("La vigencia debe ser de al menos un día.");
        }
        this.diasVigencia = diasVigencia;
    }

    public int getDiasVigencia() {
        return diasVigencia;
    }

    @Override
    public String getTipo() {
        return "Proforma";
    }

    @Override
    public double calcularTotal() {
        return calcularSubtotal() * (1 + getImpuesto() / 100);
    }

    @Override
    public int signoInventario() {
        return 0;
    }

    @Override
    public double montoParaCaja() {
        return 0;
    }
}
