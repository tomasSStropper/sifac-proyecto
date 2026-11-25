package demos;

import sifac.comun.entidades.ExistenciasInsuficientesException;
import sifac.comun.entidades.Producto;

public class DemoCarreraInventario {
    static final int EXISTENCIAS = 1000;
    static final int HILOS = 8;
    static final int INTENTOS_POR_HILO = 250;
    static final int RONDAS = 3;

    interface Inventario {
        boolean vender(int cantidad);
        int existencias();
    }

    static class InventarioSinSincronizar implements Inventario {
        private int existencias = EXISTENCIAS;

        @Override
        public boolean vender(int cantidad) {
            if (cantidad > existencias) {
                return false;
            }
            Thread.yield();
            existencias -= cantidad;
            return true;
        }

        @Override
        public int existencias() {
            return existencias;
        }
    }

    static class InventarioSincronizado implements Inventario {
        private int existencias = EXISTENCIAS;

        @Override
        public synchronized boolean vender(int cantidad) {
            if (cantidad > existencias) {
                return false;
            }
            Thread.yield();
            existencias -= cantidad;
            return true;
        }

        @Override
        public synchronized int existencias() {
            return existencias;
        }
    }

    static class ProductoSifac implements Inventario {
        private final Producto producto = new Producto("PVC-050", "Tubo PVC 1/2", 1000, EXISTENCIAS);

        @Override
        public boolean vender(int cantidad) {
            try {
                producto.descontar(cantidad);
                return true;
            } catch (ExistenciasInsuficientesException e) {
                return false;
            }
        }

        @Override
        public int existencias() {
            return producto.getExistencias();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String modo = args.length > 0 ? args[0].toLowerCase() : "ambos";
        System.out.println("Inventario inicial " + EXISTENCIAS + " unidades; " + HILOS + " cajas intentan vender "
                + INTENTOS_POR_HILO + " unidades cada una (" + HILOS * INTENTOS_POR_HILO + " intentos).");
        if (!modo.equals("con")) {
            System.out.println();
            System.out.println("ANTES: sin synchronized");
            for (int i = 1; i <= RONDAS; i++) {
                correr(i, new InventarioSinSincronizar());
            }
        }
        if (!modo.equals("sin")) {
            System.out.println();
            System.out.println("DESPUÉS: el mismo código con synchronized");
            for (int i = 1; i <= RONDAS; i++) {
                correr(i, new InventarioSincronizado());
            }
            System.out.println();
            System.out.println("SIFAC: Producto.descontar() real");
            for (int i = 1; i <= RONDAS; i++) {
                correr(i, new ProductoSifac());
            }
        }
    }

    static void correr(int ronda, Inventario inventario) throws InterruptedException {
        int[] ventasPorHilo = new int[HILOS];
        Thread[] cajas = new Thread[HILOS];
        for (int h = 0; h < HILOS; h++) {
            final int indice = h;
            cajas[h] = new Thread(() -> {
                for (int k = 0; k < INTENTOS_POR_HILO; k++) {
                    if (inventario.vender(1)) {
                        ventasPorHilo[indice]++;
                    }
                }
            }, "caja-" + (h + 1));
        }
        for (Thread caja : cajas) {
            caja.start();
        }
        for (Thread caja : cajas) {
            caja.join();
        }
        int vendidas = 0;
        for (int ventas : ventasPorHilo) {
            vendidas += ventas;
        }
        int existencias = inventario.existencias();
        boolean cuadra = vendidas + existencias == EXISTENCIAS && vendidas <= EXISTENCIAS && existencias >= 0;
        System.out.printf("  ronda %d: ventas aceptadas %4d | existencias finales %4d | vendidas de más %4d | %s%n",
                ronda, vendidas, existencias, Math.max(0, vendidas - EXISTENCIAS),
                cuadra ? "cuadra" : "NO CUADRA");
    }
}
