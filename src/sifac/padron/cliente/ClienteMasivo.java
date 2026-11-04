package sifac.padron.cliente;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClienteMasivo {

    public static void main(String[] args) throws IOException, InterruptedException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5051;
        int cantidadHilos = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        String rutaCedulas = args.length > 2 ? args[2] : "cedulas.txt";

        if (cantidadHilos < 1) {
            System.out.println("La cantidad de hilos debe ser mayor a cero.");
            return;
        }

        List<String> cedulas = leerCedulas(rutaCedulas);
        System.out.println("ClienteMasivo · " + cedulas.size() + " cedulas · "
                + cantidadHilos + " hilos");
        System.out.println();

        long inicio = System.currentTimeMillis();

        List<ConsultaLote> lotes = repartir(cedulas, cantidadHilos, puerto);
        List<Thread> hilos = new ArrayList<>();
        for (ConsultaLote lote : lotes) {
            Thread hilo = new Thread(lote, lote.getNombre());
            hilos.add(hilo);
            hilo.start();
        }
        for (Thread hilo : hilos) {
            hilo.join();
        }

        long tiempoTotal = System.currentTimeMillis() - inicio;

        int totalEncontradas = 0;
        int totalNoEncontradas = 0;
        for (ConsultaLote lote : lotes) {
            if (lote.getError() != null) {
                System.out.printf(" %s  error: %s%n", lote.getNombre(), lote.getError());
            } else {
                System.out.printf(" %s  %d cedulas   encontradas %d   no encontradas %d   t= %d ms%n",
                        lote.getNombre(), lote.getCantidad(), lote.getEncontradas(),
                        lote.getNoEncontradas(), lote.getTiempo());
            }
            totalEncontradas += lote.getEncontradas();
            totalNoEncontradas += lote.getNoEncontradas();
        }

        System.out.println();
        System.out.println(" total encontradas     " + totalEncontradas);
        System.out.println(" total no encontradas   " + totalNoEncontradas);
        System.out.println(" tiempo total          " + tiempoTotal + " ms");
    }

    private static List<String> leerCedulas(String ruta) throws IOException {
        List<String> cedulas = new ArrayList<>();
        for (String linea : Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8)) {
            if (!linea.trim().isEmpty()) {
                cedulas.add(linea.trim());
            }
        }
        return cedulas;
    }

    private static List<ConsultaLote> repartir(List<String> cedulas, int cantidadHilos, int puerto) {
        List<ConsultaLote> lotes = new ArrayList<>();
        int base = cedulas.size() / cantidadHilos;
        int sobrantes = cedulas.size() % cantidadHilos;
        int desde = 0;
        for (int i = 0; i < cantidadHilos; i++) {
            int tamano = base + (i < sobrantes ? 1 : 0);
            List<String> parte = new ArrayList<>(cedulas.subList(desde, desde + tamano));
            lotes.add(new ConsultaLote("hilo-" + (i + 1), puerto, parte));
            desde += tamano;
        }
        return lotes;
    }
}
