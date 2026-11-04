package sifac.padron.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ConsultaLote implements Runnable {

    private final String nombre;
    private final int puerto;
    private final List<String> cedulas;
    private int encontradas;
    private int noEncontradas;
    private long tiempo;
    private String error;

    public ConsultaLote(String nombre, int puerto, List<String> cedulas) {
        this.nombre = nombre;
        this.puerto = puerto;
        this.cedulas = cedulas;
    }

    @Override
    public void run() {
        long inicio = System.currentTimeMillis();
        try (Socket socket = new Socket("localhost", puerto);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            for (String cedula : cedulas) {
                salida.println(cedula);
                String respuesta = entrada.readLine();
                if (respuesta == null) {
                    throw new IOException("el servidor cerró la conexión");
                }
                if (respuesta.startsWith("OK")) {
                    encontradas++;
                } else {
                    noEncontradas++;
                }
            }
            salida.println("SALIR");
        } catch (IOException e) {
            error = e.getMessage();
        }
        tiempo = System.currentTimeMillis() - inicio;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cedulas.size();
    }

    public int getEncontradas() {
        return encontradas;
    }

    public int getNoEncontradas() {
        return noEncontradas;
    }

    public long getTiempo() {
        return tiempo;
    }

    public String getError() {
        return error;
    }
}
