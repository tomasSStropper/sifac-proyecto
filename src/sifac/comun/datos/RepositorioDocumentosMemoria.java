package sifac.comun.datos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import sifac.comun.entidades.Documento;

public class RepositorioDocumentosMemoria implements RepositorioDocumentos {
    private static final Comparator<Documento> POR_MONTO =
            Comparator.comparingDouble(Documento::calcularTotal);

    private final List<Integer> numeros = new ArrayList<>();
    private final List<Documento> documentos = new ArrayList<>();

    @Override
    public synchronized void guardar(Documento documento) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        int posicion = Collections.binarySearch(numeros, documento.getNumero());
        if (posicion >= 0) {
            throw new IllegalArgumentException("Ya existe el documento N.º " + documento.getNumero() + ".");
        }
        int insercion = -(posicion + 1);
        numeros.add(insercion, documento.getNumero());
        documentos.add(insercion, documento);
    }

    @Override
    public synchronized Documento buscarPorNumero(int numero) {
        int posicion = Collections.binarySearch(numeros, numero);
        return posicion >= 0 ? documentos.get(posicion) : null;
    }

    @Override
    public synchronized List<Documento> listar() {
        return new ArrayList<>(documentos);
    }

    @Override
    public synchronized List<Documento> listarPorMonto(boolean descendente) {
        List<Documento> copia = new ArrayList<>(documentos);
        Comparator<Documento> criterio = descendente ? POR_MONTO.reversed() : POR_MONTO;
        Collections.sort(copia, criterio.thenComparing(Comparator.naturalOrder()));
        return copia;
    }
}
