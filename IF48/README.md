# IF48 — arco, chorus y créditos; revisión del End y del stronghold

**[Descargar el mod completo IF48](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF48/ViaFabricPlus-Bedrock-IF48.jar)**

**[Leer la revisión individual de los 246 elementos](auditoria/report/REVISION-UNO-POR-UNO.md)**

Esta entrega incorpora tres correcciones comprobadas en código y pruebas dirigidas. **El combate completo del dragón todavía no está certificado**: la revisión encontró limitaciones de fases/animaciones y riesgos de selección de partes que siguen pendientes. El informe distingue cada resultado de una prueba dentro del servidor.

## Cambios

- **Arco en primera persona:** usa el modelo Java del arco en ambas manos, con sus fotogramas de tensado. El renderer nativo anterior seleccionaba siempre el fotograma de reposo, que no incluye la flecha. La decisión se mantiene coherente durante el equipamiento y el uso; conserva las rutas anteriores de mapas y ballestas. No se ejecutó el dibujo dentro de Minecraft ni se certifican modelos personalizados de packs del servidor.
- **Plantas de chorus:** reconstruye sus conexiones en la representación Java a partir de tallos/flores vecinos y piedra del End debajo. Se comprobaron carga inicial, cambios y bordes de chunks. No modifica crecimiento, inventario ni la paleta del servidor.
- **Créditos del End:** traduce el inicio de créditos y devuelve a Bedrock la confirmación al cerrarlos. El addon anterior descartaba ese mensaje. Se conservan la reaparición por muerte y las protecciones contra respuestas duplicadas/obsoletas. El destino de retorno lo decide el servidor; el viaje real completo sigue pendiente de prueba.

Conserva las correcciones anteriores de IF46 y el diagnóstico limitado de XP de IF47. Es el JAR completo, sin actualizador. Requiere mantener ViaFabricPlus base.

## Instalación

1. Cierra Minecraft y PrismLauncher por completo.
2. Abre la carpeta `mods` de tu instancia.
3. Retira el addon **ViaFabricPlus Bedrock IF** anterior y coloca este JAR. Deja **un solo addon Bedrock IF** y conserva ViaFabricPlus base.
4. Abre Minecraft y comprueba en Mods: **ViaFabricPlus Bedrock - IF48**, versión `1.1.1-StackAnvil-InteractionFix48.1`.
5. Comprueba el arco al tensarlo, también si está encantado. Las modificaciones afectan la representación; las flechas, el daño y el consumo siguen dependiendo del servidor.

## Qué se revisó

La lista individual cubre el stronghold, acceso y marcos/portal, lepismas y generador, bloques infestados, salas y bibliotecas; el dragón, cristales, aliento, partes, fases, muerte y reaparición; Enderman, endermite, shulker y proyectiles; islas, pilares, gateways, ciudades/barcos, materiales, inventarios, botines, élitros, cohetes, perlas, fruta de chorus, efectos, líquidos, vacío y otras mecánicas compartidas. El JSON y CSV conservan las mismas 246 filas e identificadores.

Las pruebas nuevas de las tres correcciones suman **60 grupos dirigidos**: 18 de arco, 21 de chorus y 21 de créditos. Se comprobaron además mappings reales de 739 estados, 89 bloques, 17 tipos de entidades, 66 objetos, 40 sonidos, 18 partículas y 7 efectos, junto con rutas concretas de entidades y objetos. Estos conteos tienen alcances distintos y no equivalen a mecánicas jugadas.

No se ejecutaron el cliente gráfico, el motor Mixin ni un servidor Bedrock. Los resultados de conversión se separan de generación, IA, daño, colisiones y aceptación del servidor en cada fila del informe.

## Pendientes relevantes

- Dragón: proyección de fases y evento de muerte; identificación de partes vanilla condicionada por el cliente y por las hitboxes que publique el servidor. La segunda pasada detectó también fallback de recursos estándar. El informe incluye los detalles y las pruebas que reproducen estos huecos.
- Enderman: señal visual de enfado en la representación Java y fallback de recursos estándar.
- XP: **sigue sin corregir**. IF48 conserva el observador de IF47; no concede ni fuerza la recogida de experiencia. Para investigarla, entra al servidor, reproduce enseguida durante unos 45 segundos y envía `logs/latest.log`. Las líneas conservan el prefijo `[IF47-XP]`. Hay una ventana por conexión; vuelve a conectar para repetirla. No hace falta otro vídeo.
- Mapas: el fallo visual anterior continúa pendiente.
- Portales completos, reaparición del dragón, vuelo, teletransportes y otras acciones listadas requieren confirmación dentro del servidor.

El catálogo usa muestras oficiales Bedrock 1.26.50.4 fijadas por commit, cercanas al objetivo del puente 1.26.51, protocolo 2193. La versión exacta y los packs del servidor pueden cambiar el contenido. Las fuentes y los límites están documentados; no se incorpora el mundo del usuario a esta revisión ni a esta descarga.
