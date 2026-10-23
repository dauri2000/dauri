# ViaFabricPlus Bedrock IF38

[Descargar el actualizador ZIP](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF38/Actualizador-a-IF38.zip)

Actualización acumulada del addon que incluye las correcciones anteriores y los cambios comprobados de mesa, yunque, aldeanos y predicción del crafteo. **Puedes generarla directamente con tu IF35**; el actualizador también admite IF36 y el IF34 original.

1. Cierra Minecraft, descarga el ZIP y extrae su contenido en una carpeta nueva.
2. Abre `ACTUALIZAR-IF38.bat` y selecciona tu `ViaFabricPlus-Bedrock-IF35.jar` actual, o el IF36 si ya lo instalaste.
3. Espera a que aparezca `ViaFabricPlus-Bedrock-IF38.jar` junto al actualizador.
4. Mueve el addon anterior fuera de `mods` y copia allí el IF38. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.

El actualizador conserva el archivo seleccionado, funciona sin conexión y verifica todas las entradas del mod generado. Requiere Java 17 o superior; Minecraft Java 26.3 y estos mods requieren Java 25 o superior. `LEEME.txt` incluye el uso por consola si Windows no encuentra Java.

Cambios incluidos:

- **Mesa:** mantiene sincronizadas las nueve casillas y sus identificadores al recibir actualizaciones completas o individuales, procesar respuestas y reabrir la mesa. Conserva las actualizaciones recientes ante rechazos de peticiones anteriores. Mantiene el relleno de recetas de IF36.
- **Yunque:** incorpora la aplicación de libros compatibles a objetos ya encantados. El caso Eficiencia II + Reparación I conserva los encantamientos, daño y otros metadatos; consume un libro y calcula coste 2 más trabajo previo. Las otras reparaciones mantienen el algoritmo anterior.
- **Aldeanos:** corrige las referencias a pilas modificadas varias veces al rellenar los costes de una oferta y evita anunciar equipo predicho antes de enviar la petición. Conserva precios, usos y las protecciones de solicitudes pendientes.
- **Crafteo rápido:** evita anunciar como equipado el resultado predicho antes de enviar la petición de fabricación. Las comprobaciones de dos clics seguidos cubren éxito y rechazo, conservando los resultados ya confirmados. La misma protección se aplica al resultado del yunque.

La validación nueva incluye 12 casos del yunque, 18 del crafteo, 7 de aldeanos y una comprobación del envío del resultado del yunque. Las nueve comprobaciones de sincronización de IF37 y diez del planificador IF36 se conservan como evidencia histórica de código retenido. Detalles, fuentes y límites en `VERIFICACION.json` y en los paquetes de cambios incluidos en el mod.

Falta comprobar el resultado en el servidor del usuario. Las respuestas simuladas no demuestran la causa completa de cualquier pérdida de objetos reportada. En el yunque, los encantamientos nuevos sin reglas verificadas siguen rechazándose en la vista previa; los soportados están documentados en las fuentes.

**Mapas:** el icono verde y azul coincide con la textura original de Java 26.3. El problema reportado de la imagen al sostener el mapa sigue pendiente de una captura; no se presenta como corregido en esta entrega.

Se conserva la corrección del escudo y el diagnóstico histórico con prefijo `[IF35 recipe-placement]`. No se añaden laboratorios ni se modifica el mundo.
