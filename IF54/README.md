# IF54.2 — bloques invisibles en Bedrock 1.26.60.6

[Descargar el mod completo IF54](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF54/ViaFabricPlus-Bedrock-IF54.jar)

El fuego, la redstone y el chorus de Bedrock 1.26.60 añadieron datos que faltaban en la tabla anterior. El addon los convertía en aire. Un bloque invisible también podía ocupar el espacio e impedir colocar tierra, una shulker u otros bloques: el servidor rechazaba la colocación y conservaba el objeto en el inventario.

IF54.2 reconoce esos bloques, conserva sus conexiones nativas y mantiene la lectura de los datos anteriores de los chunks. También conserva la optimización de cofres: en seis aperturas calientes del laboratorio, la preparación bajó de una mediana de 95,7 a 70,4 ms; no se promete eliminar toda pausa ni reproducir el rendimiento de otro equipo.

## Instalación

1. Cierra Minecraft.
2. En PrismLauncher, abre la carpeta Minecraft de Vega y entra en `mods`.
3. Sustituye el addon Bedrock anterior por `ViaFabricPlus-Bedrock-IF54.jar`. Deja un solo addon Bedrock activo.

Mantiene Minecraft Java 26.3, Fabric 0.19.5 y ViaFabricPlus base 5.1.2-SNAPSHOT. Es el JAR completo: no requiere ejecutar un actualizador. Conserva la dirección del servidor que ya utilizas; si tu servidor usa NetherNet, su formato es `nethernet://TU_IP:TU_PUERTO`.

## Qué se comprobó

Se comparó el catálogo completo de 1.480nombres del protocolo2225 y se consultaron dentro del cliente los 23.960registros del catálogo. Los 13identificadores desconocidos del registro recibido correspondían a fuego y redstone. El informe de datos y procedencia está en [BLOQUES-REVISION.json](BLOQUES-REVISION.json).

En el cliente final se comprobaron los modelos cargados y sus categorías. También se vieron fuego tras un rayo, redstone alimentada y chorus, se colocaron tierra, una shulker y un tronco por la ruta normal del juego, y se transfirieron nueve troncos entre inventario y cofre sin pérdidas. [Comprobaciones del archivo final](COMPROBACIONES.json). Fluidos, bloques técnicos invisibles y bloques representados por entidades se clasifican por sus rutas especiales; la consulta de modelos no es una prueba de todas sus mecánicas, todas las texturas o cada variante colocada en un mundo.

Los tres tipos `icicle`, `ice_crystal` y `disc_press` aparecen en el catálogo del protocolo, pero los comandos del servidor del usuario y del BDS de prueba los rechazan. Sus 30registros permanecen explícitamente no compatibles; no se inventaron modelos ni se cambió Java para representarlos.

Se mantienen las 44dependencias internas y todas las clases del addon anterior, incluidos HUD57 y la corrección de pistones. Las revisiones históricas de XP, mapas, recetas, End y stronghold conservan los límites documentados en sus informes; esta entrega no las declara concluidas.

## Integridad y fuentes

SHA256 del JAR completo: `ea29d43b1f76717c7dd6c03587433f41194a139e3d0eee8552fe91f9ee9980d3`.

[Fuentes incrementales de los cambios propios](ViaFabricPlus-Bedrock-IF54-owned-sources.zip). Conservan la procedencia de las tablas; no incluyen el cliente Minecraft, mundos, registros del usuario ni observadores privados del laboratorio.
