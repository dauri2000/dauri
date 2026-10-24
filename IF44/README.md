# ViaFabricPlus Bedrock IF44

[Descargar el mod completo IF44](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF44/ViaFabricPlus-Bedrock-IF44.jar)

IF44 corrige una restricción que impedía seleccionar con el cubo vacío fuentes de agua contenidas en la segunda capa de bloques Bedrock cuando su equivalente Java no tiene la interfaz de recogida de líquidos. El caso se reprodujo con tolvas, puertas y puertas de valla. La fuente existía en los datos nativos y otros usos podían encontrarla, pero el cubo se descartaba por la propiedad Java.

1. Cierra Minecraft.
2. Descarga `ViaFabricPlus-Bedrock-IF44.jar`.
3. Saca de `mods` el addon Bedrock anterior y guárdalo como respaldo.
4. Pon el JAR IF44 en `mods`, sin descomprimir. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
5. Reinicia y comprueba en Mods **ViaFabricPlus Bedrock - IF44**, versión `InteractionFix44.1`.
6. Prueba recoger esa agua con un cubo vacío en la mano principal.

Es el mod completo, sin actualizador ni versiones intermedias. Requiere Minecraft Java 26.3 y Java 25 o superior. Conserva las confirmaciones de colocación de IF43, la compatibilidad de materiales de IF42 y las correcciones anteriores.

## Cambio y comprobaciones

La búsqueda conserva las comprobaciones existentes de alcance, fuente y obstáculos. Además de los casos Java ya admitidos, el cubo puede seleccionar agua nativa con `liquid_depth=0` en la segunda capa de la misma celda y con bloque primario conocido. Las fuentes detrás de una pared, el agua corriente y las celdas sin agua siguen sujetos a sus comprobaciones originales. La acción se envía a Bedrock; el inventario y el agua cambian con la respuesta autoritativa del servidor.

El parche se limita a dos llamadas de la búsqueda de fluidos y a un helper nuevo. Las clases cliente, los estados del mundo, las paletas, las confirmaciones IF43 y el resto del JAR se conservan por hash. `VERIFICACION.json` recoge el alcance, los hashes y las pruebas; las fuentes y las reproducciones seleccionadas están en `META-INF/stackanvil-if44-changes.zip`.

Pasaron 29 comprobaciones dirigidas y siete pruebas independientes del handler de uso del cubo, con los mismos dos hashes finales. La revisión ASM conserva todos los métodos fuera de las dos llamadas y sus cargas de celda, y recupera canónicamente IF43 al retirar el parche. Las pruebas de diagnóstico anteriores y las comparaciones IF42/IF43 se registran aparte y no se suman a esas 36 comprobaciones.

Las pruebas usan los modelos, las formas y los handlers reales de ViaBedrock con límites de mundo y transporte en memoria. No se ejecutó Minecraft gráfico ni un servidor real. No se ha confirmado el bloque exacto de la captura del usuario; falta comprobar el resultado en su servidor. La representación visual de bloques que Java no permite mojar sigue teniendo limitaciones: esta entrega corrige la selección y el envío de recogida de fuentes nativas.

La comparación de cubos normales entre IF42 e IF43 no reprodujo una regresión permanente de recogida. La restricción corregida ya estaba presente en IF43 y versiones anteriores. No se incluyen mundos, vídeos ni logs privados del usuario.
