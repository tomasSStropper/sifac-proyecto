package sifac.comun.entidades;

public class ExistenciasInsuficientesException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private final String codigoProducto;
    private final int disponibles;
    private final int solicitadas;

    public ExistenciasInsuficientesException(String codigoProducto, int disponibles, int solicitadas) {
        super("Existencias insuficientes de " + codigoProducto + ": hay " + disponibles
                + " y se pidieron " + solicitadas + ".");
        this.codigoProducto = codigoProducto;
        this.disponibles = disponibles;
        this.solicitadas = solicitadas;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public int getSolicitadas() {
        return solicitadas;
    }
}
