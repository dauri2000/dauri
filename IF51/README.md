# IF51-TEST1 — prueba de la desconexión

Estado: mod completo preparado y probado en el laboratorio. La descarga de prueba todavía no se ha publicado; falta que el usuario pueda repetir el fallo en su servidor y aportar el nuevo registro.

El registro del 26 de octubre confirma dos desconexiones con IF50.1 al reconectar. El cliente interpreta cantidades de 202 y 162 atributos y las rechaza porque su lista admite como máximo 128. El error posterior de `debug/block_value` aparece durante el cierre del canal. El usuario confirmó que apareció una pantalla de desconexión. Los registros no contienen el cuerpo del paquete rechazado ni identifican de forma explícita el momento del portal.

El candidato incluye:

- Una corrección del emisor de atributos: divide las listas grandes en paquetes de hasta 128 entradas, conservando todos los atributos, su orden, valores y modificadores. No aumenta el límite del cliente ni descarta entradas.
- La corrección del inventario que quedaba vacío visualmente tras la segunda reaparición al volver del End.
- Un diagnóstico opcional del paquete rechazado, anterior a su decodificación. Solo observa `UPDATE_ATTRIBUTES` de una conexión Bedrock 2193 cuando Java está en PLAY y la cantidad declarada supera 128. No altera los bytes, índices, decisiones ni errores del decoder. Registra hasta 8 KiB del paquete, con indicador de truncamiento, una vez por conexión y como máximo cuatro veces por ejecución. La división de atributos puede registrar hasta cuatro líneas adicionales de cantidades, sin contenido de objetos.

## Comprobaciones

Las pruebas ejecutan el handler nativo y el codec real de Java 26.3. Las listas controladas de 162 y 202 entradas reproducen el error en IF50; el candidato las entrega como `[128,34]` y `[128,74]`, respectivamente. Se conservaron todos sus valores y modificadores. Los paquetes originales de hasta 128 atributos permanecen idénticos por bytes. Estas entradas controladas demuestran un defecto del límite; no demuestran todavía que el servidor del usuario enviara ese contenido.

El JAR completo arrancó en Fabric y se conectó al BDS oficial 1.26.52.3 del laboratorio. La salida real por un bloque de portal, con posicionamiento administrativo de preparación, regresó al Overworld conservando 13 stacks y 29 objetos; BDS confirmó los 13 tipos y cantidades. No hubo errores de atributos ni activaciones falsas del diagnóstico en ese recorrido. Se verificaron 125 comprobaciones de 61 entradas: 60 clases por su origen real y el mixin registrado por su recurso exacto; su aplicación se contrastó además en el `PacketDecoder` transformado. El cierre de esa ejecución fue normal, con código 0.

El laboratorio sigue usando NetherNet. La conexión RakNet del usuario, sus otros mods y el paquete defectuoso exacto requieren la prueba en su entorno. Hay un plan y un helper RakNet preparados, pero todavía no se han ejecutado. Tampoco se consideran completas las revisiones de todos los crafteos, el End, el stronghold ni los sonidos. Los candidatos separados de reparación de cobre y variantes de sonidos no se integran en esta prueba.

## Artefacto preparado

- Versión: `1.1.1-StackAnvil-InteractionFix51-TEST1`.
- Tamaño del mod completo: 216171953 bytes.
- SHA256: `d1cfa0e3919917ecdebcb613cdc9df7d3ad4213b9cf65596c58d179159245c05`.
- Se conservan las 45 dependencias anidadas; solo cambia el core entre ellas. Los otros 44 JAR son idénticos a IF50.

El diagnóstico está desactivado por defecto. La prueba requiere añadir en los argumentos Java de la instancia Prism:

```text
-Dviabedrock.attributeDecodeDiagnostics=true
```

Si se publica para la prueba, se entregará el JAR completo mediante GitHub, sin actualizador. El IF50 publicado se conserva intacto. Esta versión todavía no se presenta como solución confirmada de la desconexión del usuario.
