# ViaFabricPlus Bedrock IF39

[Descargar el mod completo IF39](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF39/ViaFabricPlus-Bedrock-IF39.jar)

Un solo **Shift + clic** continúa fabricando por lotes después de confirmar cada petición anterior. IF38 se detenía tras el primer lote aunque quedaran materiales; IF39 continúa hasta agotar los materiales de la cuadrícula o el espacio que permite el crafteo normal. La comprobación local cubre 128 tablones → 256 palos con un solo gesto, tanto en la mesa como en la cuadrícula del jugador.

1. Cierra Minecraft y descarga directamente `ViaFabricPlus-Bedrock-IF39.jar`.
2. Mueve el addon Bedrock anterior fuera de `mods` y guarda ese archivo como respaldo.
3. Copia el IF39 descargado a `mods`. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
4. Abre Minecraft con tu instancia habitual.

La descarga es el JAR completo, sin ejecutar actualizadores ni instalar versiones intermedias. Minecraft Java 26.3 y estos mods requieren Java 25 o superior.

Incluye las correcciones de IF38 del escudo, mesa, yunque, aldeanos y predicción del crafteo. La continuación se detiene ante rechazos, cierre de pantalla, cambios de receta o cambios externos de los objetos. Una nueva acción manual tiene prioridad. Conserva las reglas anteriores de espacio y del cursor; no añade reparto del resultado entre varios huecos parcialmente ocupados.

Pasaron **26 comprobaciones nuevas** con los manejadores y la cola reales, respuestas simuladas y recuperación tras un tiempo de espera agotado. Cubren los cuatro lotes de fabricación, capacidad, rechazos, cancelaciones y acciones manuales. Los cinco archivos de clase empaquetados coinciden por SHA256 con los probados. El JAR conserva las demás entradas de IF38; contiene fuentes, revisión y resultados en `META-INF/stackanvil-if39-changes.zip`. Detalles en `VERIFICACION.json`.

Las pruebas históricas de IF36–IF38 se conservan como evidencia de los cambios anteriores; no se volvieron a ejecutar para esta entrega. Falta comprobar el resultado en el servidor del usuario y en una sesión gráfica de Minecraft. El problema de la imagen del mapa al sostenerlo continúa pendiente y no se presenta como corregido.
