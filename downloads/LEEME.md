# IF54: movimiento e inventario Java — versión experimental

Este archivo modifica exclusivamente el JAR IF54.2 que proporcionaste. Conserva el
identificador Fabric `viafabricplus-bedrock`; sustituye el original, no cargues ambos
a la vez. Mantiene sus requisitos: Minecraft **26.3**, Java **25 o superior** y
ViaFabricPlus **>=5.0.2**, con una versión de ViaFabricPlus compatible con esa base.
La revisión histórica de la base menciona 5.1.2-SNAPSHOT; no se verificó aquí su
compatibilidad con todas las versiones que admite el manifiesto.

## Cambios

- Usa las rutas Java originales para la locomoción del jugador local sin montura:
  postura, agachado, sprint, natación, escalada, vuelo, planeo y respuesta física
  personal, conservando las comunicaciones del protocolo Bedrock.
- Los efectos de movimiento de la miel se separan por entidad; su geometría de
  bloque sigue siendo la de la base Bedrock.
- Los controles y menús ordinarios de inventario permanecen Java: se evita su
  sustitución por las pantallas nativas Bedrock. Se mantiene el adaptador de
  cartografía para no perder resultados específicos del servidor.
- Se mantienen los permisos impuestos por el servidor, las correcciones de
  posición, el envío de predicciones, las transacciones y los cierres de inventario.
- Las monturas y los demás componentes Bedrock conservan las implementaciones de
  la base. Los inventarios de cofres en barcos/vagonetas usan controles Java; esto
  no convierte su conducción a Java.

No transforma Minecraft Java en una implementación completa de Bedrock. Recetas,
objetos permitidos y resultados siguen sujetos al servidor Bedrock. Si este
rechaza diferencias de movimiento Java, puede corregir la posición del jugador.

## Comprobaciones realizadas

- Reconstrucción de las once partes y extracción del 7z completadas sin error.
- Integridad CRC del JAR de salida y de todas sus dependencias JAR anidadas.
- Validación estructural y de flujo básico del bytecode mediante ASM 9.9.
- 16 clases exteriores modificadas: 47 métodos previstos cambian y 56 conservan
  sus instrucciones y metadatos.
- En `InventoryPackets`, únicamente cambia la presentación nativa: los otros
  58 métodos permanecen idénticos. La apertura ordinaria sigue fluyendo una vez.
- 31 aserciones aisladas verifican la política de movimiento, separación de
  monturas, postura, anidamiento, aislamiento entre hilos y limpieza ante errores.
  Se ejecutaron con Java 25 y el `PlayerPosture` real de ViaBedrock, usando dobles
  de prueba para Minecraft/Fabric/Mixin. **No son pruebas dentro del juego.**
- No se empaquetan clases auxiliares de compilación ni se sustituyen otras
  dependencias, recursos, predicciones de vehículos o correcciones del servidor.

## Pendiente dentro de Minecraft

No se lanzó Minecraft/Fabric ni se abrió una sesión contra un servidor Bedrock.
Por tanto, no se ha comprobado la aplicación real de los nuevos mixins ni la
jugabilidad. Prueba primero en una copia de tu instancia y un mundo de prueba.

1. Caminar, diagonales, sprint, saltos, agachado, arrastre, agua y lava/corrientes.
2. Escaleras, vuelo creativo, élitros, cohetes y efectos de movimiento.
3. Montar y desmontar caballos, barcos y happy ghast; dirección y saltos; comprobar
   también cambios rápidos entre jugador sin montura y montado.
4. Abrir/cerrar inventario y cofres; clic normal/derecho, arrastre, doble clic,
   Shift, teclas numéricas, soltar objetos, recetas y fabricación; verificar que
   el resultado persista tras cerrar y volver a abrir.
5. Inventario lleno, rechazos del servidor, desconexión/reconexión, cambio de
   dimensión y portales con inventario abierto/cerrado.
6. Cambiar de servidor Bedrock a Java y comprobar los permisos de entrada.

Las consultas compartidas de colisión o fluidos fuera de los ámbitos normales de
tick/baseTick/move conservan Bedrock; su comportamiento requiere pruebas reales.
La base ya incluía una nota experimental sobre un fallo de inventario al volver
de un portal que no había sido reproducido. Este parche no declara solucionado
ese problema. No se garantiza la ausencia de fallos futuros.

## Reproducir el parche

El ZIP de fuentes incluye los cambios, las pruebas y el script de construcción.
La licencia de la base se conserva. No incluye el JAR original ni herramientas
descargadas. Los informes antiguos dentro del mod son referencias históricas,
no comprobaciones de esta modificación.

Necesitas JDK 25 o superior y los cuatro JAR de ASM **9.9** desde Maven Central,
guardados como `asm.jar`, `asm-tree.jar`, `asm-analysis.jar` y `asm-util.jar`.
No se desactiva ninguna verificación TLS ni de integridad.

Desde el directorio de las fuentes:

```sh
python3 build_hybrid.py /ruta/ViaFabricPlus-Bedrock-IF54.jar \
  --jdk /ruta/jdk-25 \
  --asm-dir /ruta/asm-9.9 \
  --output /ruta/ViaFabricPlus-Bedrock-IF54-JavaMovementInventory-EXPERIMENTAL.jar
```

El script rechaza cualquier original cuyo SHA-256 no sea:

`ea29d43b1f76717c7dd6c03587433f41194a139e3d0eee8552fe91f9ee9980d3`.

Conserva tu original para revertir: quita esta versión modificada y vuelve a
instalar el JAR original. No hace falta reemplazar el mundo ni su inventario.
