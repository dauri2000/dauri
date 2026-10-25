# ViaFabricPlus Bedrock IF46

[Descargar el mod completo IF46](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF46/ViaFabricPlus-Bedrock-IF46.jar)

Corrige la sincronización del coste en niveles del yunque. Incluye IF45 y las correcciones anteriores; no necesita actualizador.

## Qué cambia

El yunque calculaba un coste válido, pero lo enviaba a Java **antes** de actualizar los objetos. Al recalcular el resultado, el menú de Java puede sobrescribir ese valor. Se reprodujo el orden incorrecto al renombrar, recibir el inventario completo y recibir las ranuras de entrada y resultado.

IF46 envía el coste calculado después de los objetos, mediante la cola del event loop de la conexión. También lo reenvía después de resincronizar el contenido aunque los objetos no hayan cambiado. Comprueba que sigue abierto el mismo yunque, en la misma generación de menú, y descarta las tareas antiguas al cerrar o cambiar de pantalla.

La corrección se aplica a las operaciones válidas con **libros, herramientas y armaduras**. El texto, sus colores por experiencia disponible y el aviso de coste excesivo siguen a cargo de la pantalla normal de Java. No se cambian las fórmulas de coste, los encantamientos, el consumo de objetos ni la experiencia que cobra el servidor.

## Instalación

1. Cierra Minecraft por completo.
2. Quita el addon Bedrock IF45 o anterior de `mods`. Conserva el ViaFabricPlus base y los demás mods.
3. Copia `ViaFabricPlus-Bedrock-IF46.jar` en `mods`. Debe quedar **un solo addon Bedrock IF**.
4. Reinicia y comprueba que Mods muestra **IF46**, versión `1.1.1-StackAnvil-InteractionFix46.1`.
5. Prueba una combinación válida en el yunque y comprueba el texto del coste antes de retirar el resultado.

## Comprobaciones

Los informes adjuntos fijan los hashes de las clases probadas y del JAR completo. Las pruebas ejecutan el cálculo del yunque, los manejadores y emisores reales de paquetes y un event loop Netty de prueba. Cubren el orden del coste y las resincronizaciones, además de las tareas pendientes tras cerrar o reabrir un menú.

El cambio de PacketFactory se limita al punto de escritura del contenido: el método que confirma bloques y todos los demás métodos originales se conservan. Las nueve clases añadidas o modificadas en IF45 permanecen idénticas, al igual que las reglas del yunque anteriores y los fixes de agua, materiales y monturas.

**No se ejecutó Minecraft ni un servidor Bedrock real en este entorno.** La comprobación visual dentro del servidor sigue pendiente; no se presenta el orden de paquetes como una ejecución del render del cliente.
