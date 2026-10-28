# IF53 — pistones, apertura de cofres e inventario

**[Descargar ViaFabricPlus-Bedrock-IF53.jar — mod completo](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF53/ViaFabricPlus-Bedrock-IF53.jar)**

Para **Bedrock Dedicated Server 1.26.60.6**, manteniendo **Minecraft Java 26.3**, Fabric 0.19.5 y ViaFabricPlus base 5.1.2-SNAPSHOT. Versión interna: **53.2**. El JAR completo sustituye al addon Bedrock anterior; no requiere actualizador.

## Cambios y alcance

- **Cierre al renderizar pistones:** inicializa la entidad móvil con la dirección real del bloque antes de hacerla visible al cliente. Evita la ventana anterior en que una consulta de colisión podía encontrar la dirección nula. Los datos posteriores del servidor conservan su autoridad sobre dirección, progreso y estado del pistón. La corrección mantiene las condiciones existentes para conexiones Bedrock.
- **Pausa al abrir cofres:** reutiliza dentro de la misma llamada el texto de la escena ya serializado y validado. Conserva la interfaz, los controles y el límite de un MiB UTF-8. No almacena inventarios ni resultados de objetos entre aperturas. En el ensayo aislado se elimina una segunda serialización que costaba aproximadamente 5/8 ms CPU y 4,12/4,85 MB por cofre pequeño/grande; esos valores no son FPS ni tiempos del equipo del usuario. Sigue existiendo trabajo de resolución y presentación, por lo que no se promete eliminar toda pausa.
- **Recuperación del inventario:** acepta las 57 posiciones completas de la interfaz de inventario y el formato anterior de 54. Conserva las tres posiciones adicionales como datos opacos, sin inventar acciones Java para ellas. El rechazo anterior impedía recibir el cursor durante una recuperación y terminaba en desconexión. Se mantienen los plazos, las respuestas reales y los requisitos estrictos de sincronización. El desencadenante del primer timeout no queda demostrado por el registro.
- Se conservan los demás contenidos de IF52 y sus 44 dependencias anidadas ajenas al core. No se incluye el experimento privado de copias de `NativeUiScene`.

## Instalación y conexión

1. Cierra Minecraft.
2. En `minecraft/mods` de Vega, guarda fuera de esa carpeta el addon Bedrock IF anterior y coloca IF53. Debe quedar un solo addon Bedrock. Conserva ViaFabricPlus base y Java 26.3.
3. Abre la instancia y comprueba que Mods muestra **ViaFabricPlus Bedrock - IF53**, versión terminada en **53.2**.

No extraigas el JAR. Para el servidor con transporte NetherNet conserva la dirección **`nethernet://DIRECCION:PUERTO`**, sin barra final, y la versión Bedrock seleccionada. Una dirección simple usa RakNet; no equivale a NetherNet. Esta diferencia de conexión ya fue comprobada por el usuario con IF52.

## Comprobaciones

El JAR final exacto se ejecutó con Java 26.3 y BDS oficial 1.26.60.6, protocolo 2225, en un mundo propio de laboratorio mediante NetherNet. Pasaron **112 entradas / 227 comprobaciones** de origen: 109 clases cargadas y tres recursos Mixin registrados (recursos anteriores a la transformación).

Se recibieron cuatro paquetes HUD reales con contador de 57 posiciones; la revisión del inventario avanzó y desapareció el rechazo de longitud. Una transferencia normal con Mayús movió ocho troncos del cofre a uno existente: quedaron nueve. Al cerrar y reabrir, el origen seguía vacío y se conservaron los 64 bloques de piedra de la otra posición. Cinco consultas al BDS confirmaron cantidades y equipo. Después de más de dos minutos, no había solicitudes ni interacciones pendientes, y no hubo expulsión.

También se accionaron pistones normal y pegajoso con el cliente conectado. Las pruebas del cliente se hicieron sin el Sodium exacto del usuario; las consultas de colisión de Java real cubren el mecanismo del cierre informado. La ejecución final cerró normalmente con código **0**. El laboratorio no modifica el servidor, el mundo ni la cuenta del usuario.

El fallo del pistón se reprodujo de forma controlada consultando la colisión desde otra hebra entre la creación y la carga del NBT. Con las clases reales de Java 26.3 y los datos emitidos por el core, el constructor anterior falló en 24 casos; el corregido conservó una dirección válida. La carga posterior produjo el mismo estado final. Esto comprueba el mecanismo; no se presenta como ejecución del conjunto completo de mods del usuario.

Archivo completo: **216.192.193 bytes**. SHA256: `7b59b53df95661d725acc4219d6dd69e985074691ac24b7c155aacb3e48013d7`. La verificación de la copia publicada se guarda por separado en `DESCARGA-VERIFICADA.json`.

## Pendientes

La automatización exacta de Tweakeroo y el conjunto completo de mods/paquetes del usuario siguen requiriendo su prueba específica. Las revisiones exhaustivas anteriores de todos los crafteos, el End y el stronghold siguen abiertas. Esta entrega se limita a los fallos descritos y no certifica todos los problemas anteriores; se mantienen los [límites de IF52](../IF52/README.md).

[Sumas SHA256](SHA256SUMS) · [Comprobaciones](COMPROBACIONES.json) · [Descarga completa verificada](DESCARGA-VERIFICADA.json) · [Fuentes propias incrementales](ViaFabricPlus-Bedrock-IF53-owned-sources.zip)

Las fuentes incrementales incluyen las herramientas y el helper propios de esta entrega. No contienen Minecraft, BDS, mundos, registros del usuario ni controles del laboratorio; no son una compilación autónoma del proyecto completo.
