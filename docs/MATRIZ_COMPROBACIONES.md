# Matriz de comprobaciones · Entrega 1

Cada requisito, dónde está implementado y cómo se comprueba. Resultado obtenido con `mvn test`
(43 pruebas, 0 fallos) y con las ejecuciones descritas en el README.

## Requisitos funcionales

| # | Requisito | Implementación | Comprobación | Resultado |
|---|---|---|---|---|
| 1 | Emitir los cuatro tipos con su cálculo | `FacturaContado`, `FacturaCredito`, `NotaCredito`, `Proforma` · `calcularTotal()`; `TipoDocumento.crear()` | `DocumentosTest`: 10.735 / 11.526 / −10.735 / 11.300; `ServicioFacturacionTest.emiteLosCuatroTiposDeDocumento` | ✔ |
| 2 | Agregar líneas: cantidad > 0 y producto existente | `ServicioFacturacion.agregarProducto()`, `LineaDetalle` | `cantidadCeroSeRechaza`, `productoInexistenteSeRechaza`, `lineaConCantidadCeroSeRechaza` | ✔ |
| 3 | Consultar cliente por cédula contra la capa de datos | `RepositorioClientes` → `RepositorioClientesPadron` / `RepositorioClientesMemoria` | `RepositoriosTest.consultaClientePorCedulaEnElPadron`, `cedulaInexistenteDevuelveNull`, `clienteNoRegistradoSeRechaza` | ✔ |
| 4 | Descontar inventario y rechazar si no alcanza | `Producto.descontar()` (synchronized), `ExistenciasInsuficientesException` | `agregarLineaDescuentaInventario`, `existenciasInsuficientesSeRechazanSinTocarElInventario`, `elInventarioNoSeVuelveNegativo` | ✔ |
| 5 | Deshacer la última línea | `Pila<LineaDetalle>` en `ServicioFacturacion.deshacerUltimaLinea()` | `deshacerQuitaLaUltimaLineaYDevuelveElInventario`, `deshacerSinLineasDevuelveFalse` | ✔ |
| 6 | Historial de estados y su consulta | `ListaEnlazada<RegistroEstado>` en `Documento`; menú opción 6 | `elHistorialEmpiezaAbiertoYAvanzaEnOrden`, `saltarseUnEstadoSeRechaza`, `emitirCambiaElEstadoYEncola` | ✔ |
| 7 | Encolar emitidos y vaciar la cola en segundo plano | `ColaEnvio` + `ProcesadorEnvios` (hilo `procesador-envios`) | `elProcesadorConsumeTodosLosEmitidosEnOrdenYEnOtroHilo` | ✔ |
| 8 | Cierre de caja del día | `ServicioFacturacion.cierreDeCaja()` → `ResumenCierre` | `cierreDeCajaSumaElDiaYNoCuentaDineroDeProformas` | ✔ |
| 9 | Listar del día por monto y buscar por número | `listarDelDiaPorMonto()` (`Collections.sort` + `Comparator`); `buscarPorNumero()` (`Collections.binarySearch`) | `listarPorMontoOrdenaDeMayorAMenor`, `buscarPorNumeroEncuentraElEmitidoYNoInventaOtros` | ✔ |

## Requisitos técnicos

| Requisito | Implementación | Comprobación | Resultado |
|---|---|---|---|
| Clase abstracta y polimorfismo sin condicionales por tipo | `Documento` abstracta; `calcularTotal`, `montoParaCaja`, `signoInventario`, `getTipo`; `TipoDocumento` | Ningún `switch`, `instanceof` ni `getClass()` por tipo en `comun` ni `caja` | ✔ |
| Capas y regla de dependencia | `entidades`, `estructuras`, `datos`, `logica`, `caja.ui` | `entidades` no importa capas superiores; la lógica usa solo interfaces de datos | ✔ |
| Repositorios como interfaces | `RepositorioClientes`, `RepositorioProductos`, `RepositorioDocumentos` | Implementaciones en memoria y contra archivo (padrón) | ✔ |
| JUnit con casos de rechazo | 5 clases de prueba, 43 casos | `mvn test` | ✔ 43/43 |
| Lista enlazada propia para el historial | `ListaEnlazada<RegistroEstado>` | `EstructurasTest.listaEliminaCabezaYUltimoSinPerderLaCadena` | ✔ |
| Pila propia para deshacer | `Pila<LineaDetalle>` | `pilaDevuelveEnOrdenInverso`, `pilaVaciaDevuelveNull` | ✔ |
| Cola propia de pendientes de envío | `Cola<Documento>` dentro de `ColaEnvio` | `colaRespetaElOrdenDeLlegada`, `colaFuncionaDespuesDeVaciarse` | ✔ |
| Colecciones de la API con ordenamiento y búsqueda binaria | `RepositorioDocumentosMemoria`: `Collections.sort`, `Comparator`, `Collections.binarySearch` | Pruebas del punto 9 | ✔ |
| Hilo que procesa la cola mientras la caja opera | `ProcesadorEnvios` en el hilo `procesador-envios` | Historial: Emitido en `main`, Procesado en `procesador-envios` | ✔ |
| Inventario sincronizado con evidencia | `Producto.descontar()` synchronized | `docs/evidencia/*.txt`, `inventarioSincronizadoNoVendeDeMas` (30 rondas) | ✔ |
| Productor-consumidor sobre la cola compartida | `ColaEnvio`: `wait()` / `notifyAll()` | `elConsumidorEsperaSinGastarCpuYDespiertaAlEncolar` (estado WAITING) | ✔ |

## Comprobación de que la prueba de concurrencia detecta el error

Se quitó `synchronized` de `Producto.descontar()` y se corrió la prueba 5 veces: falló las 5
