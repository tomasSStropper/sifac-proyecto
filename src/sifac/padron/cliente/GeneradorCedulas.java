package sifac.padron.cliente;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GeneradorCedulas {

    private static final Charset TSE = Charset.forName("ISO-8859-15");
    private static final int EXISTENTES = 150;
    private static final int INVENTADAS = 50;
    private static final int SALTO = 20;

    public static void main(String[] args) throws IOException {
        String rutaPadron = args.length > 0 ? args[0] : "PADRON_SINTETICO.txt";
        String rutaSalida = args.length > 1 ? args[1] : "cedulas.txt";

        List<String> existentes = new ArrayList<>();
        Set<String> candidatas = new HashSet<>();
        for (int i = 1; i <= INVENTADAS * 2; i++) {
            candidatas.add(String.valueOf(900000000 + i));
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaPadron), TSE))) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                if (linea.length() < 9) {
                    continue;
                }
                String cedula = linea.substring(0, 9);
                candidatas.remove(cedula);
                if (numero % SALTO == 0 && existentes.size() < EXISTENTES) {
                    existentes.add(cedula);
                }
                numero++;
            }
        }

        if (existentes.size() < EXISTENTES) {
            throw new IllegalStateException("El padrón no tiene suficientes cédulas.");
        }

        List<String> inventadas = new ArrayList<>(candidatas);
        Collections.sort(inventadas);
        if (inventadas.size() < INVENTADAS) {
            throw new IllegalStateException("No se pudieron generar cédulas inexistentes.");
        }

        List<String> cedulas = new ArrayList<>(existentes);
        cedulas.addAll(inventadas.subList(0, INVENTADAS));
        Collections.shuffle(cedulas, new Random(2026));

        try (PrintWriter escritor = new PrintWriter(rutaSalida, "UTF-8")) {
            for (String cedula : cedulas) {
                escritor.println(cedula);
            }
        }
        System.out.println("Archivo " + rutaSalida + " generado: " + EXISTENTES
                + " existentes y " + INVENTADAS + " inventadas.");
    }
}
