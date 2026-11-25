package sifac.comun.datos;

import java.util.List;
import sifac.comun.entidades.Producto;

public interface RepositorioProductos {
    Producto buscar(String codigo);
    List<Producto> listar();
}
