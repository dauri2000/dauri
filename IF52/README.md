# IF52 — Bedrock Dedicated Server 1.26.60.6 con Minecraft Java 26.3

**[Descargar ViaFabricPlus-Bedrock-IF52.jar — mod completo](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF52/ViaFabricPlus-Bedrock-IF52.jar)**

Guardado en esta rama de GitHub. Es el addon completo, listo para sustituir el anterior, sin actualizador. Versión interna: `1.1.1-StackAnvil-InteractionFix52.1`; tamaño: **216.188.237 bytes**. Destino comprobado: servidor oficial **1.26.60.6**, protocolo **2225**. Se mantiene **Minecraft Java 26.3**, Fabric **0.19.5**, Java **25 o superior** y ViaFabricPlus base **5.1.2-SNAPSHOT**.

## Instalación

1. Cierra Minecraft y abre la carpeta `minecraft/mods` de la instancia Vega en PrismLauncher.
2. Guarda fuera de `mods` el addon `ViaFabricPlus-Bedrock-IF…jar` anterior y coloca el JAR IF52 descargado. Debe quedar un solo addon Bedrock. Conserva el mod base ViaFabricPlus.
3. Abre la misma instancia Java 26.3. En Mods debe aparecer **ViaFabricPlus Bedrock - IF52**, versión terminada en **52.1**.

No extraigas el JAR. Se instala en el cliente Fabric; no se instala como plugin del servidor Bedrock. Los originales y las entregas anteriores se conservan en el repositorio.

## Cambios

- Adaptación a los campos de protocolo 2225 de entrada al mundo, dimensiones, chunks, skins, lista de jugadores, animaciones e interacciones con objetos y entidades.
- Actualización de las variantes de solicitudes de inventario y normalización de ingredientes que incluyen datos auxiliares en el nombre, conservando el contador y los datos auxiliares separados.
- Adaptación del formato nativo de eventos de sonido y de la identidad de recursos y cachés a 1.26.60. Se conserva el formato interno de sonidos existente.
- Corrección de la sincronización de los élitros: cuando Bedrock comunica que el jugador dejó de planear, el traductor actualiza su estado interno. Se conserva la lógica anterior de inicio del planeo.
- Conservación de las correcciones de IF51 para atributos y reenvío de inventario al volver del End. La composición final conserva tanto esas correcciones como los campos de mano nuevos.

## Pruebas realizadas

Se utilizó una copia de laboratorio del servidor oficial **1.26.60.6, build 52886190**, un mundo nuevo y una conexión normal desde Java 26.3 mediante NetherNet. No se modificó el servidor, el mundo ni la cuenta del usuario.

- **JAR final:** arranque y entrada al mundo con el archivo descargable exacto. Se comprobaron **107 entradas y 217 condiciones** de carga: 105 clases con su origen de carga y dos recursos Mixin registrados. Las **44 dependencias anidadas ajenas al core** permanecen idénticas.
- **Élitros en supervivencia:** equipamiento manual, despegue mediante dos pulsaciones de salto y movimiento, planeo sostenido, impulso con cohetes, aterrizaje y desactivación del planeo. El uso mantenido consumió tres cohetes, de seis a tres, y aumentó la velocidad. El desgaste observado pasó de 59 a 96 sobre 432; Java y Bedrock coincidieron en los estados estables.
- **Pechera y nuevo despegue:** cambio manual a pechera de diamante, restauración de los élitros y segundo despegue aceptado. Consultas al servidor confirmaron los élitros equipados, la pechera conservada y los tres cohetes restantes.
- **Crafteo:** cuatro tablones y seis cohetes de vuelo 1 con un solo Mayús-clic en una ejecución previa de esta adaptación. Los resultados persistieron al reconectar y el servidor confirmó las cantidades. No equivale a comprobar todas las recetas.
- **Salida del End:** contacto con un portal preparado en laboratorio, pantalla de créditos y retorno al Overworld sin desconexión. Se conservaron cuatro pilas con doce objetos y se confirmaron por consultas al servidor. Esta prueba usó TEST2, cuyo core y código de producción son idénticos a IF52; cambiaron los metadatos de presentación. No acredita igualdad de todos los datos NBT ni una victoria natural contra el dragón.
- Las ejecuciones TEST2 y final cerraron normalmente con código **0**. La telemetría y los controles del laboratorio quedan fuera del mod publicado.

## Alcance pendiente

La automatización de cambio de pechera al saltar de **Tweakeroo 0.30.1+viafabric.compat1** necesita el JAR y su configuración exactos para comprobarse. El cambio manual y el vuelo sí se probaron. Tampoco se certifican aquí vuelo con la última durabilidad, reparación/Mending ni todas las condiciones de vuelo. El conjunto completo de mods y paquetes del usuario no estuvo disponible en este laboratorio.

La revisión de **todos los crafteos, todo el End y el stronghold** sigue abierta. También siguen abiertos los fallos anteriores que no tienen una prueba específica en esta entrega, incluidos XP, mapas, ghast feliz, agua y colocación de bloques. La adaptación de sonidos no acredita que todos se escuchen correctamente. Los nuevos pasajeros de bloques y sus poses/cámara no están implementados por completo. El prototipo opcional del índice nuevo del cortapiedras no está integrado ni se presenta como comprobado.

## Integridad y fuentes

SHA256 del mod: `be93f17052182a9658e019e77da23c26b1ec44d80a686ee40f3b2bf1adee71b8`.

[Descarga completa verificada](DESCARGA-VERIFICADA.json) · [Sumas SHA256](SHA256SUMS) · [Resumen de comprobaciones](COMPROBACIONES.json) · [Fuentes propias de las modificaciones](ViaFabricPlus-Bedrock-IF52-owned-sources.zip)

El ZIP de fuentes contiene 33 archivos propios y su manifiesto; es un reproductor parcial de las modificaciones, no una distribución del código completo ni una compilación autónoma. No incluye binarios de Minecraft o BDS, mundos, registros del usuario ni herramientas privadas del laboratorio.
