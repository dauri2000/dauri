# End: criaturas, combate y entidades — auditoría IF46

**Actualización:** la [segunda pasada](findings-segunda-pasada.md) ejecutó la resolución/apariencia estándar: dragón y Enderman caen al renderer original por causas reproducidas. Añade 13 afirmaciones sobre apariencia y picking. Esa precisión posterior prevalece sobre las reservas generales de esta primera tabla acerca de qué renderer podría activarse.

Base estable inspeccionada: el JAR completo IF46 con SHA256 `a0d21a7476ccd2c4ebf5570efd8c3474ae3ea66015c84054c0040532faf57ea8`. IF47 añade observación de XP; no es una corrección de combate ni de recogida. No se modificó ninguna clase de producción en esta auditoría.

Se ejecutaron **58 afirmaciones** sobre clases reales del core: **53 comprueban conversiones/modelos con datos suministrados y 5 reproducen riesgos o huecos**. No equivalen a 58 mecánicas verificadas jugando. `probe-output.txt`, `EndEntityAuditProbe.java` y `verification.json` contienen las pruebas y hashes. La conexión, transporte y arranque de registros son fronteras explícitas del ensayo. No hay cliente Minecraft, motor Mixin ni servidor BDS ejecutándose.

## Lista comprobada individualmente

| Elemento o mecánica | Resultado y alcance de la verificación |
|---|---|
| Dragón: tipo de entidad | El mapping entregado tiene `ender_dragon → ENDER_DRAGON`; el tracker real mantiene UID, RID y Java ID distintos. Ensayado. |
| Dragón: posición | El modelo conserva las coordenadas nativas sin el desplazamiento ocular del jugador. Ensayado. Esto no verifica interpolación gráfica. |
| Dragón: hitboxes nativas | Al suministrar un HITBOX válido, se crean proxies INTERACTION; el ID se resuelve al dragón. Ensayado con una caja controlada. No demuestra que el servidor del usuario envíe esa metadata. |
| Dragón: interacción con hitboxes nativas | El clic relativo se convierte a la posición absoluta nativa, el proxy se mueve con el dragón y sus aliases se retiran al eliminarlo, sin eliminar al cristal siguiente. Cuatro afirmaciones reales de la ruta del tracker. |
| Dragón: ataque cuerpo a cuerpo | El handler ATTACK usa `getEntityByJid`; la ruta de proxy anterior tiene dueño correcto. **Pendiente** el ataque real contra las partes vanilla y la aceptación del servidor. |
| Dragón: partes vanilla | **Riesgo reproducido**: no hay reserva de IDs ni aliases específicos para las partes; el actor siguiente recibe `dragonId + 1`. No se conoce el layout exacto de IDs del cliente 26.3 a partir de un cliente ejecutable; no se propone un offset/cantidad inventados. |
| Dragón: flechas y proyectiles | Identidad/posición de `dragon_fireball` y owner UID comprobados; los proyectiles que dispara el jugador y el daño efectivo pertenecen a la auditoría de objetos. Inmunidades y acierto del servidor no ejecutados. |
| Dragón: barra de jefe | Handler real BOSS_EVENT comprobado para Add, Update_Percent y Remove; usa UID del jefe para su UUID Java y resetea el estado al retirarla. No se comprobó su dibujo en pantalla. |
| Dragón: vida y daño | LivingEntity transmite salud/atributos recibidos; el cálculo de daño y regeneración es autoridad del servidor. No se ensayó una pelea ni se da por validado el valor recibido de 200 HP. |
| Dragón: aterrizar, posarse y despegar | **Hueco de proyección estática**: existe el campo Java PHASE en los datos entregados, pero el conversor de metadata no lo escribe para el dragón. Movimiento recibido no equivale a animación de fase correcta. |
| Dragón: carga, vuelo y elección de blanco | IA controlada por Bedrock; recepción de actor/movimiento disponible. No se ejecutaron esas fases. |
| Dragón: aliento y nubes | `area_effect_cloud` tiene mapping correcto; DATA_RADIUS 2.5 y DATA_WAITING se proyectan a sus campos Java. Partículas, forma visible, daño, duración y recogida con botella no se deducen de este ensayo. |
| Dragón: bola de aliento | Tipo y posición comprobados; OWNER se busca por UID, no RID. Acierto/explosión/daño deben provenir del servidor. |
| Dragón: destrucción de bloques | El servidor decide qué bloques destruye; updates de mundo se auditan por separado. No se simuló la colisión del dragón con bloques. |
| Dragón: animación de muerte | **Hueco reproducido en handler real**: DRAGON_START_DEATH_ANIM se cancela. No se traduce al campo Java PHASE ni a un evento equivalente en esa ruta. No significa que el dragón no pueda morir en el servidor. |
| Dragón: muerte, huevo, portal y gateway | Decisiones/creación del servidor. No se inventan recompensas ni generación desde el cliente; los paquetes de mundo/dimensión se revisan por separado. Muerte y regeneración completas pendientes de ejecución. |
| Dragón: XP de la muerte | La recogida de XP **sigue sin resolver**; IF47 registra el caso del usuario. Las pruebas anteriores de ADD/MOVE/REMOVE y atributos de XP no prueban que las bolitas se recojan a tiempo. |
| Dragón: reaparición con cuatro cristales | No ejecutada. Requiere interacción de colocación, consumo confirmado, reconstrucción de pilares/cristales, animación de invocación y aparición/estado de jefe. No se marca correcta por existir los mappings. |
| Cristal: entidad y posición | Mapping `ender_crystal → END_CRYSTAL`, identidad y posición ensayados. |
| Cristal: base visible | Flag SHOW_BOTTOM convertido a boolean Java. Ensayado. |
| Cristal: rayo a un destino | BLOCK_TARGET no nulo se convierte a BEAM_TARGET; `(0,0,0)` lo retira según la política existente. Ambos ensayados. No verifica la curación real del dragón. |
| Cristal: romper/explosión/daño | Autoridad Bedrock; ataque y actualización de mundo disponibles, pero explosión real no ejecutada. |
| Enderman: entidad y posición | Mapping e identidades/posición ensayados. |
| Enderman: enfado y mirada | **Hueco reproducido**: ANGRY no genera CREEPY ni STARED_AT para la representación Java. El renderer de recursos Bedrock puede tener otra ruta; esta prueba no garantiza qué renderer usa la sesión real. |
| Enderman: coger bloques | El campo Java CARRY_STATE existe; no hay caso para DEPRECATED_CARRY_BLOCK_RUNTIME_ID en el conversor inspeccionado. Es una ausencia de proyección estática, no prueba de que el backend actual envíe el dato deprecated. |
| Enderman: teleport, agua, calabaza, agresión, perlas y botín | Decisiones de IA, validación y daño del servidor. No se ejecutaron. Metadata/movimiento no prueban los disparadores de IA. |
| Endermite | Mapping/identidad/posición ensayados. Aparición por perla, duración, agresión y botín son de servidor y no se ejecutaron. |
| Shulker: entidad y posición | Mapping/identidad/posición ensayados. |
| Shulker: orientación | Las seis ATTACH_FACE se convierten sin cambiar el eje. Seis afirmaciones. No es una prueba gráfica de la caja en cada cara. |
| Shulker: abrir/cerrar | PEEK 0/30/100 se conserva como byte. Tres afirmaciones. |
| Shulker: color | Valores nativos 0/7/15/16 coinciden con la política actual `15 − variant`, o 16 sin color. Se prueba la política del conversor; no la paleta contra cada versión de BDS. |
| Shulker: armadura cerrado, teleport y duplicación | Comportamiento y daño de servidor; no ejecutados. No se importan reglas Java al servidor Bedrock. |
| Shulker: muerte y caparazón | Actor y botín se reciben por rutas genéricas; botín/cantidad no simulados ni garantizados. |
| Bala de shulker | Mapping/identidad/posición y OWNER por UID comprobados; usar RID como UID no resuelve un owner accidental. Seguimiento, destrucción y colisión física reales pendientes. |
| Levitación | `LivingEntity.updateEffect` transmite ID real, amplificador y duración; removeEffect limpia la caché. Ensayado para levitación. No prueba movimiento/física del jugador en juego. |
| Caída lenta | La misma ruta comprobada con efecto slow_falling y retirada. Puede introducirse al End mediante poción; no aparece por generación natural. |
| Daño instantáneo del aliento | Ruta de efecto comprobada con instant_damage. No determina el daño de la nube, que puede llegar como daño/salud autorizado y no como efecto persistente. |
| Silverfish del stronghold | Mapping/identidad/posición ensayados. Activación de spawner, salida de bloques infestados y ataque pertenecen al servidor; bloques/spawner revisados en la auditoría de mundo. |

## Código concreto y restricciones

Las fuentes de referencia originales IF34 de estas clases siguen siendo útiles para leer métodos; `verification.json` fija los bytes del core IF46 realmente ejecutado. Los hooks de XP añadidos después no se usan para afirmar comportamiento de esta prueba.

- `protocol/packet/EntityPackets.java:143`: ADD_ENTITY, tipo/posición/velocidad/metadata, y entitySpawned.
- `protocol/storage/EntityTracker.java:79`: selección de modelos; `:489` lookup de IDs y proxies; `:531` contador de un único ID por llamada.
- `protocol/storage/InteractionHitboxProxies.java:29`: registro, dimensiones, movimiento, aliases y retirada de las cajas nativas.
- `protocol/packet/ClientPlayerPackets.java:730`: ATTACK cancela IDs sin dueño y envía RID del dueño encontrado.
- `protocol/rewriter/EntityMetadataRewriter.java:105`: SHOW_BOTTOM; `:827` caras y PEEK de shulker; `:857` radio y espera de nube; `:912` rayo del cristal.
- `protocol/packet/EntityPackets.java:535`: ENTITY_EVENT y su fallback; `:888` lista de equivalentes Java sin DRAGON_START_DEATH_ANIM.
- `protocol/packet/HudPackets.java:461`: boss bar por UID → actor UUID.
- `api/model/entity/LivingEntity.java:189`: transmisión/retirada de efectos.
- Outer `render/BedrockActorPicking.java`: selección mediante proxies y dueños; `BedrockActorPickingState` exige UUID/lifetime válidos.
- Outer `render/BedrockWorldActorRenderer.java:94`: puede sustituir el renderer de entidades vivas si hay recursos soportados; si falla usa el renderer original. Eso impide convertir una ausencia de metadata Java en afirmación universal de fallo gráfico.

La referencia exacta NeoForge **26.3.0-stable**, commit `68e75538286a318981e17c517b08457419ef7ef4`, confirma que EnderDragon es multipart, devuelve subEntities y define PHASE como HOVERING al inicio. Su patch parcial no muestra asignación de IDs de las partes. La fuente histórica de mahtomedi se conserva únicamente para orientar investigación: muestra ocho partes y una asignación que empieza en el ID del padre; **no se usa como ABI 26.3 ni como justificación de un fix**.

Las muestras oficiales Bedrock descargadas son de la rama pública main, su `format_version` actual puede diferir del servidor del usuario. Se guardan URL y SHA256 en `reference/manifest.json`; las reglas IA que describen no se consideran una prueba de la sesión.

## Reproducción

Requiere compilar el harness de transporte reutilizado `ExperienceOrbLifecycleProbe.java` (SHA en verification.json) y sus dependencias reales. La lista de classpath guardada referencia la copia IF46 del core en primer lugar, antes de copias anteriores.

```sh
/workspace/minecraft/tools/jdk-25.0.5.0.2/bin/javac -cp "$(cat classpath.txt)" -d test-classes EndEntityAuditProbe.java
/workspace/minecraft/tools/jdk-25.0.5.0.2/bin/java -Xverify:all -cp "test-classes:$(cat classpath.txt)" EndEntityAuditProbe
```

No se entregó ni publicó un mod corregido de combate como parte de esta subauditoría. Se prioriza resolver con evidencia los riesgos de hitboxes/fases/muerte y la XP ya reportada antes de declarar listo un combate completo.
