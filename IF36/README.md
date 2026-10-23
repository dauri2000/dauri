# ViaFabricPlus Bedrock IF36: rellenado de recetas

[Descargar el actualizador ZIP](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF36/Actualizador-a-IF36.zip)

Corrige el rechazo del libro de recetas cuando ya hay ingredientes en la mesa y quedan pocos huecos en el inventario. Antes, cada casilla ocupada de la mesa exigía un hueco vacío distinto, incluso si sus ingredientes podían apilarse con los del inventario. IF36 apila las devoluciones compatibles y reutiliza los ingredientes que ya están en la casilla adecuada.

Conserva la corrección del escudo incluida en IF35. Es un mod del cliente para jugar desde Minecraft Java 26.3 en un servidor Bedrock. Conserva también el JAR base de ViaFabricPlus.

1. Cierra Minecraft, descarga el ZIP y extrae su contenido en una carpeta nueva.
2. Abre `ACTUALIZAR-IF36.bat` y selecciona tu `ViaFabricPlus-Bedrock-IF35.jar`. También acepta el IF34 original.
3. Espera a que termine. El nuevo `ViaFabricPlus-Bedrock-IF36.jar` aparece junto al actualizador.
4. Mueve el addon anterior fuera de `mods` y copia allí el IF36. Mantén instalado ViaFabricPlus base y un solo addon IF34/IF35/IF36.

El actualizador funciona sin conexión, conserva el archivo seleccionado y comprueba todos los contenidos del JAR generado. Requiere Java 17 o superior; Minecraft y estos mods requieren Java 25 o superior. Si Windows no encuentra Java, el archivo `LEEME.txt` explica cómo usar el Java de Prism Launcher.

La causa del registro se reprodujo con las clases originales. Las comprobaciones locales cubren la planificación y los movimientos de ingredientes; la confirmación dentro del servidor queda pendiente. Los detalles y límites de validación están en `VERIFICACION.json` y en las fuentes incluidas.

Se conserva el diagnóstico de IF35, incluido su prefijo `[IF35 recipe-placement]`, por si vuelve a rechazarse alguna receta. No se añaden laboratorios ni se modifica el mundo.
