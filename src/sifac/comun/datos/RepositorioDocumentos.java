package sifac.comun.datos;

import java.util.List;
import sifac.comun.entidades.Documento;

public interface RepositorioDocumentos {
    void guardar(Documento documento);
    Documento buscarPorNumero(int numero);
    List<Documento> listar();
    List<Documento> listarPorMonto(boolean descendente);
}
