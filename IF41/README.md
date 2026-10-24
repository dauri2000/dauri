# ViaFabricPlus Bedrock IF41

[Descargar el mod completo IF41](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF41/ViaFabricPlus-Bedrock-IF41.jar)

IF41 corrige dos rutas locales del Happy Ghast: la posición interna del jinete podía quedarse en el origen durante el vuelo y descartar muestras nativas válidas; si se descargaba el chunk de ese origen, un paquete de rotación podía enviar al jugador hacia la posición antigua. El parche mantiene al piloto sincronizado con muestras nuevas válidas y comprueba el chunk de la montura actual. Conserva las correcciones explícitas del servidor y el desmontaje de IF40.

1. Cierra Minecraft y descarga `ViaFabricPlus-Bedrock-IF41.jar`.
2. Mueve el addon Bedrock anterior fuera de `mods` y guárdalo como respaldo.
3. Copia el IF41 descargado a `mods`. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
4. Reinicia la instancia y **comprueba en Mods que aparece `ViaFabricPlus Bedrock - IF41`**, con versión `InteractionFix41.1`, antes de probar el vuelo. La versión cargada durante el reporte anterior no está confirmada.

La entrega es el JAR completo, sin actualizador ni versiones intermedias. Minecraft Java 26.3 y estos mods requieren Java 25 o superior.

Pasaron **15 comprobaciones dirigidas y cuatro independientes**, con los mismos cuatro hashes finales. Las independientes ejecutan el manejador completo de entrada: conserva posición actual, velocidad nativa y frame; mantiene la autoridad de correcciones de vehículo y jugador, rechaza epochs antiguos y conserva las dos órdenes de desmontaje pendiente de IF40. La revisión ASM conserva los métodos originales fuera de las inserciones aprobadas.

Solo se modifican tres clases y se añade un helper. `HappyGhastEntity` de IF40 permanece idéntico por SHA256, igual que el resto de entradas no autorizadas. Fuentes, reproducciones, revisiones y resultados están en `META-INF/stackanvil-if41-changes.zip`; `VERIFICACION.json` detalla los hashes y separa las 15 y cuatro comprobaciones. Las suites históricas se conservan como evidencia y no se repitieron.

Las pruebas usan manejadores reales con límites de conexión/salida y disponibilidad de chunks en memoria; la entrada de desmontaje pendiente se prepara por reflexión. No se inició Minecraft gráfico ni un servidor real. Falta comprobar el resultado en el servidor del usuario; los resultados locales no prueban que el servidor acepte toda la trayectoria de vuelo.

Los problemas de bloques y del observador orientado hacia abajo siguen pendientes, sin parche en esta entrega. XP y la imagen del mapa al sostenerlo también continúan pendientes. No se incluirán vídeos ni logs privados del usuario.
