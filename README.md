# SIFAC - Entrega 1

Proyecto programado de Programación III , ULatina.
Carlos Tomás Tropper Azofeifa.

SIFAC es un sistema para facturar en una ferretería: el cajero busca al cliente por su cédula, arma
la factura, el sistema controla cuánto queda de cada producto y al final del día saca el cierre de caja.
Esta es la primera entrega; la conexión entre varias cajas y un servidor central viene en la segunda.

## Qué se necesita

- JDK 17 o más nuevo
- NetBeans o Maven
- Internet la primera vez, para que Maven baje JUnit

## Cómo correrlo

En NetBeans abro la carpeta del proyecto (la que tiene el `pom.xml`) y desde el proyecto:

- Run: abre el menú de la caja. La clase principal es `sifac.caja.ui.Principal`.
- Test: corre las pruebas (son 43).
- Para la condición de carrera: clic derecho a `src/demos/DemoCarreraInventario.java` → Run File.

Desde la terminal:

```
mvn test
mvn compile exec:java
mvn compile exec:java -Dexec.mainClass=demos.DemoCarreraInventario
```

Por ahora no hay orden de arranque porque es un solo programa. En la Entrega 2 el orden va a ser:
servidor de padrón, servidor central y después las cajas.

## Datos

- Clientes: se buscan por cédula en `PADRON_SINTETICO.txt` (3000 personas inventadas, ninguna es real).
  Para probar uso `104560789`, que también se puede escribir con guiones.
- Productos: están en memoria en `RepositorioProductosMemoria`. El martillo (`MAR-16`) tiene solo
  2 unidades a propósito, para probar el rechazo por existencias.
- El padrón real no se sube al repositorio, está en el `.gitignore`.

## Qué debería pasar

- Factura de contado con 10 tubos: 10735.00 (5 % de descuento y 13 % de impuesto).
- La misma de crédito: 11526.00. Nota de crédito: -10735.00. Proforma: 11300.00.
- Pedir 3 martillos: lo rechaza porque solo hay 2.
- Al emitir, el documento pasa a "Emitido" y unos 3 segundos después a "Procesado". En el historial
  se ve que ese último cambio lo hizo el hilo `procesador-envios` y no el `main`.
- El cierre de caja muestra la proforma, pero no la suma, porque una proforma no es dinero.
- `DemoCarreraInventario`: sin `synchronized` se venden más de 1000 unidades y el inventario queda
  negativo; con `synchronized` siempre son 1000 exactas.

## Cómo está organizado

```
sifac
├── comun
│   ├── entidades     Documento y sus 4 tipos, Producto, Cliente, estados
│   ├── estructuras   lista enlazada, pila y cola hechas por mí
│   ├── datos         repositorios (interfaces y sus implementaciones)
│   └── logica        ServicioFacturacion, la cola de envío y el hilo que la procesa
├── caja.ui           el menú de consola
└── padron            el servidor de padrón de los laboratorios
```

La interfaz solo habla con la lógica, y la lógica solo conoce las interfaces de los repositorios.
El único lugar donde se escogen las clases concretas es el `main` de `Principal`.

Dónde está cada cosa que pide el enunciado:

- Deshacer: la pila en `ServicioFacturacion.deshacerUltimaLinea`.
- Historial: lista enlazada dentro de `Documento`.
- Cola de envío y productor/consumidor: `ColaEnvio` (con `wait` y `notifyAll`) y `ProcesadorEnvios`.
- Inventario sincronizado: `Producto.descontar`.
- Orden por monto y búsqueda binaria: `RepositorioDocumentosMemoria`.
- Sin `if` ni `switch` por tipo de documento: cada tipo calcula su total y el enum `TipoDocumento` sabe crear cada uno.

La evidencia de la condición de carrera está en `docs/evidencia` (antes y después) y la lista de
requisitos con sus pruebas en `docs/MATRIZ_COMPROBACIONES.md`.

## Limitaciones que noté

- Todavía no hay red: el "envío" es un cambio de estado después de una espera de 3 segundos.
- Todo vive en memoria; al cerrar se pierde lo facturado.
- Si cancelo un documento, su número se pierde y la numeración queda con un hueco. En la Entrega 2
  el número lo va a dar el servidor central.
- La nota de crédito devuelve inventario sin revisar contra qué factura se hace.
- Las pruebas de hilos dependen de cómo se repartan los hilos, así que encuentran el error con mucha
  probabilidad pero no con certeza.

## Lo que sigue (Entrega 2)

Conectar el padrón por socket con una clase nueva de repositorio sin tocar la lógica, hacer el
servidor central que atienda varias cajas a la vez, mandar los documentos por socket y agregar el
tipo de cambio del BCCR con su caché.
