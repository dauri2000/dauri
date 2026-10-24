# ViaFabricPlus Bedrock IF40

[Descargar el mod completo IF40](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF40/ViaFabricPlus-Bedrock-IF40.jar)

IF40 evita que el desmontaje del Happy Ghast recupere una posición antigua después del vuelo predicho localmente. Si llega una corrección nueva del servidor, conserva su prioridad, incluso cuando ya se había solicitado bajar. El cambio se limita a `HappyGhastEntity` y conserva el resto de IF39.

1. Cierra Minecraft y descarga `ViaFabricPlus-Bedrock-IF40.jar`.
2. Mueve el addon Bedrock anterior fuera de `mods` y guárdalo como respaldo.
3. Copia el IF40 descargado a `mods`. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
4. Abre Minecraft con tu instancia habitual.

La entrega es el JAR completo, sin actualizador ni versiones intermedias. Minecraft Java 26.3 y estos mods requieren Java 25 o superior.

La reproducción local demostró el origen antiguo de desmontaje en IF39 y una segunda carrera entre la solicitud de bajar y una corrección del vehículo. Pasaron **nueve comprobaciones independientes** con la misma clase final y Java 25, usando los enlaces, el desmontaje y el manejador de movimiento reales. Cubren predicción, correcciones de teleport/force, movimiento sin control local, ciclos de montar y las dos órdenes de corrección durante un desmontaje pendiente. La entrada pendiente de Shift se prepara por reflexión en esas pruebas.

La revisión ASM verifica que el resto de los métodos conserva su contenido anterior. El JAR contiene las fuentes y los informes nuevos en `META-INF/stackanvil-if40-changes.zip`; `VERIFICACION.json` detalla hashes y límites. La evidencia histórica de IF39 y anteriores se conserva sin repetir sus regresiones.

No se inició Minecraft gráfico ni un servidor real. El vídeo revisado muestra vuelo, sin mostrar el salto final reportado; la reproducción del defecto fue local. Falta comprobar el resultado contra el servidor del usuario. El problema de la imagen del mapa al sostenerlo sigue pendiente y no se modifica en IF40.

**XP:** sigue pendiente un vídeo que muestre ese problema. El vídeo revisado del Happy Ghast no demuestra un fallo de XP. Los vídeos y logs privados del usuario no forman parte de esta entrega.
