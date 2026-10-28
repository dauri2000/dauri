# Adaptación a Bedrock v26.60 — en preparación

El servidor de destino confirmado por el usuario es **Bedrock Dedicated Server 1.26.60.6**, según la línea de versión de la consola de ScalaCube. La actualización debe mantener **Minecraft Java 26.3**, Java >=25, ViaFabricPlus base 5.1.2-SNAPSHOT y las correcciones de IF51. Los archivos originales siguen intactos. Todavía no hay un nuevo JAR completo ni una descarga de IF52.

## Trabajo preparado y comprobado por separado

- Parsers de entrada al mundo, dimensiones y chunks: incorporan campos nuevos. Pruebas de manejadores reales contrastan IF51 con el prototipo. DIMENSION_DATA se ejecuta completo; las pruebas de StartGame y LevelChunk se limitan a los prefijos afectados y no acreditan sus cuerpos o semánticas completos.
- Transacciones de uso y liberación de objetos: conservan el campo de mano añadido. El tipo real pasa 112 casos controlados y 592 comprobaciones; los emisores de Java a Bedrock también requieren adaptación y revisión separadas.
- Skins y lista de jugadores: el cambio de posición de PlayFabID se adapta de forma conjunta. Las pruebas del tipo y del manejador cubren identidad, plataforma, datos de imagen y cuerpos truncados.
- Actualización de sonidos: se adapta el formato nativo de un evento y se conserva el formato interno existente. Las pruebas del tipo real cubren siete variantes y sus límites; no son una validación audible en el juego.
- Herramientas de conservación: verifican los cambios por entrada y hash, las 44 dependencias anidadas ajenas al core y los metadatos Java 26.3. Los prototipos contrastan sus cambios aislados con las instrucciones originales; falta comprobar la conservación semántica de las correcciones de IF51 en el addon integrado.

Estas piezas son prototipos privados, todavía sin integrar en un addon distribuible. No representan compatibilidad completa con v26.60.

## Comprobaciones pendientes para la entrega

El tag oficial de documentación v1.26.60 y Cloudburst v2225 describen el protocolo **2225**. La documentación contiene la etiqueta `1.26.60-beta.0`; las revisiones Preview anteriores de 26.60 usan otros protocolos. Se confirmó después la build completa del servidor, 1.26.60.6; falta ejecutar esa build para contrastar su protocolo y datos reales.

La tabla numérica de acciones de inventario de la documentación y la de Cloudburst discrepan. Esa tabla no se ha cambiado por suposición. También faltan el catálogo nativo actualizado, los cambios de selección de recetas y pasajeros de bloques, y la comprobación completa de los emisores de interacciones y animaciones.

Para ejecutar la conexión, el inventario y el mundo con ese destino se necesita el **ZIP limpio de Bedrock Dedicated Server 1.26.60.6 para Linux**. Las consultas a las API primaria y secundaria y a la página oficial de BDS devolvieron CONNECT403 del proxy incluso después del permiso adicional. El intento posterior del archivo exacto, con otro permiso explícito, también fue bloqueado: no se recibió el ZIP ni se acreditó su disponibilidad HTTP. El laboratorio disponible continúa siendo BDS 1.26.52.3, compatible con el IF51 probado; no acredita la versión nueva. No se ha conectado ni modificado el servidor del usuario.

La revisión general del End, el stronghold, todos los crafteos y los fallos anteriores sigue abierta. Esta adaptación de versión tampoco la declara completa. Se conserva intacta la [descarga anterior IF51-TEST1](../IF51/README.md), con su alcance y pendientes documentados.
