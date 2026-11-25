package sifac.comun.datos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sifac.comun.entidades.Producto;

public class RepositorioProductosMemoria implements RepositorioProductos {
    private final Map<String, Producto> productos = new LinkedHashMap<>();

    public RepositorioProductosMemoria() {
        agregar(new Producto("PVC-050", "Tubo PVC 1/2", 1000, 100));
        agregar(new Producto("CEM-50", "Cemento gris 50 kg", 8500, 40));
        agregar(new Producto("CLA-2", "Clavos 2 pulgadas (kg)", 1200, 200));
        agregar(new Producto("PIN-GL", "Pintura látex galón", 14500, 25));
        agregar(new Producto("TOR-1", "Tornillos 1 pulgada (caja)", 2300, 60));
        agregar(new Producto("MAR-16", "Martillo 16 oz", 6500, 2));
    }

    public RepositorioProductosMemoria(List<Producto> iniciales) {
        for (Producto producto : iniciales) {
            agregar(producto);
        }
    }

    private void agregar(Producto producto) {
        productos.put(producto.getCodigo(), producto);
    }

    @Override
    public Producto buscar(String codigo) {
        return codigo == null ? null : productos.get(codigo.trim().toUpperCase());
    }

    @Override
    public List<Producto> listar() {
        return new ArrayList<>(productos.values());
    }
}
