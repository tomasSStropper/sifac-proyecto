package sifac.comun.entidades;

public enum EstadoDocumento {
    ABIERTO("Abierto"),
    EMITIDO("Emitido"),
    PROCESADO("Procesado");

    private final String descripcion;

    EstadoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean puedePasarA(EstadoDocumento siguiente) {
        return siguiente != null && siguiente.ordinal() == ordinal() + 1;
    }
}
