# ViaFabricPlus Bedrock IF43

[Descargar el mod completo IF43](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF43/ViaFabricPlus-Bedrock-IF43.jar)

IF43 corrige dos rutas de confirmación adelantada encontradas al revisar la colocación rápida a mano. La actualización del bloque que sirve de apoyo podía confirmar también el siguiente bloque pendiente; además, una acción posterior podía confirmar una secuencia mayor saltándose esa espera. Estas confirmaciones podían retirar predicciones del cliente antes de recibir el resultado correspondiente. La revisión se realizó sin vídeos.

1. Cierra Minecraft.
2. Descarga `ViaFabricPlus-Bedrock-IF43.jar`.
3. Saca de `mods` el addon Bedrock anterior y guárdalo como respaldo.
4. Pon IF43 en `mods`. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
5. Reinicia y comprueba en Mods **ViaFabricPlus Bedrock - IF43**, versión `InteractionFix43.1`.
6. Prueba colocar una fila de bloques seguidos a mano, como cuando aparecían los huecos.

Es el JAR completo, sin actualizador ni versiones intermedias. Requiere Minecraft Java 26.3 y Java 25 o superior. Conserva la selección de materiales de Litematica/MaLiLib de IF42 y todas las entradas anteriores fuera del alcance explícito del parche.

## Cambio y comprobaciones

Las colocaciones de bloques se siguen por su destino previsto. Una actualización del apoyo compartido deja de confirmar destinos distintos que aún no han recibido actualización. Las confirmaciones directas posteriores esperan detrás de las predicciones anteriores pendientes; el tick libera el prefijo listo una sola vez. Los intentos de colocación y las demás acciones se siguen enviando con su orden y contenido originales.

Se conserva el límite anterior de un segundo para liberar predicciones sin respuesta. En colocaciones que sustituyen el bloque clicado o unen losas, la confirmación puede esperar hasta ese límite si la actualización llega solo al bloque clicado. Las actualizaciones autoritativas siguen aplicándose. Las interacciones con objetos que no son bloques y el seguimiento de cubos conservan sus rutas anteriores.

`VERIFICACION.json` recoge las pruebas dirigidas, los hashes, el alcance exacto de los cambios y la conservación del resto del JAR. Las fuentes, reproducciones antes/después y revisiones seleccionadas están en `META-INF/stackanvil-if43-changes.zip`.

Pasaron 18 comprobaciones dirigidas y dos independientes sobre los mismos cinco hashes finales. La reproducción original falla con IF42 y pasa con la corrección. La revisión ASM verifica los métodos y recupera canónicamente las clases IF42 al retirar únicamente las inserciones aprobadas. Las pruebas históricas se conservan como evidencia y no se repitieron.

Las pruebas ejecutan handlers, modelos, cola y envío de confirmaciones reales de ViaBedrock con límites de red y mundo en memoria. No se simula un servidor que rechace los intentos para fabricar una reproducción. La referencia Java 26.3 disponible confirma conservación y restauración de estados de predicción, pero no incluye todos los cuerpos del manejador. No se ejecutó Minecraft gráfico ni un servidor real; falta comprobar el resultado en el servidor del usuario.

La auditoría también encontró descripciones de cantidad y runtime que pueden quedarse antiguas entre respuestas, sin demostrar que causen rechazos de colocación. Esos campos no forman parte de este parche. Litematica no quedó identificada como causa necesaria del fallo manual. No se incluyen mundos, vídeos ni logs privados del usuario.
