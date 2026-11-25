package sifac.comun.logica;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sifac.comun.datos.RepositorioClientesMemoria;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioProductosMemoria;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;
import sifac.comun.entidades.ExistenciasInsuficientesException;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.RegistroEstado;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrenciaTest {

    @Test
    void inventarioSincronizadoNoVendeDeMas() throws InterruptedException {
        for (int ronda = 1; ronda <= 30; ronda++) {
            Producto tubo = new Producto("PVC-050", "Tubo PVC 1/2", 1000, 1000);
            int hilos = 8;
            int intentos = 500;
            int[] ventas = new int[hilos];
            int[] rechazos = new int[hilos];
            Thread[] cajas = new Thread[hilos];
            for (int h = 0; h < hilos; h++) {
                final int indice = h;
                cajas[h] = new Thread(() -> {
                    for (int k = 0; k < intentos; k++) {
                        try {
                            tubo.descontar(1);
                            ventas[indice]++;
                        } catch (ExistenciasInsuficientesException e) {
                            rechazos[indice]++;
                        }
                    }
                });
            }

            for (Thread caja : cajas) {
                caja.start();
            }
            for (Thread caja : cajas) {
                caja.join();
            }

            int vendidas = 0;
            int rechazadas = 0;
            for (int h = 0; h < hilos; h++) {
                vendidas += ventas[h];
                rechazadas += rechazos[h];
            }
            assertEquals(1000, vendidas, "ronda " + ronda);
            assertEquals(hilos * intentos - 1000, rechazadas, "ronda " + ronda);
            assertEquals(0, tubo.getExistencias(), "ronda " + ronda);
        }
    }

    @Test
    void elProcesadorConsumeTodosLosEmitidosEnOrdenYEnOtroHilo() throws InterruptedException {
        ColaEnvio cola = new ColaEnvio();
        ServicioFacturacion servicio = new ServicioFacturacion(new RepositorioClientesMemoria(),
                new RepositorioProductosMemoria(), new RepositorioDocumentosMemoria(), cola);
        servicio.registrarCliente("1-1111-1111", "Rafael Mora");
        ProcesadorEnvios procesador = new ProcesadorEnvios(cola, 0);
        Thread consumidor = new Thread(procesador, "procesador-envios");
        consumidor.start();

        List<Integer> emitidos = new ArrayList<>();
        List<Documento> documentos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Documento d = servicio.crearDocumento(TipoDocumento.FACTURA_CONTADO, "1-1111-1111");
            servicio.agregarProducto(d, "CLA-2", 1);
            servicio.emitir(d);
            emitidos.add(d.getNumero());
            documentos.add(d);
        }
        cola.cerrar();
        consumidor.join(5000);

        assertFalse(consumidor.isAlive());
        assertEquals(emitidos, procesador.getProcesados());
        for (Documento d : documentos) {
            assertEquals(EstadoDocumento.PROCESADO, d.getEstado());
            List<RegistroEstado> historial = d.getHistorial();
            assertEquals("procesador-envios", historial.get(historial.size() - 1).getHilo());
        }
    }

    @Test
    void elConsumidorEsperaSinGastarCpuYDespiertaAlEncolar() throws InterruptedException {
        ColaEnvio cola = new ColaEnvio();
        ProcesadorEnvios procesador = new ProcesadorEnvios(cola, 0);
        Thread consumidor = new Thread(procesador, "procesador-envios");
        consumidor.start();
        Thread.sleep(200);

        assertEquals(Thread.State.WAITING, consumidor.getState());

        cola.cerrar();
        consumidor.join(2000);
        assertFalse(consumidor.isAlive());
    }
}
