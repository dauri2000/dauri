# End y stronghold: objetos, inventario y acciones

Base: JAR completo IF46, SHA256 `a0d21a7476ccd2c4ebf5570efd8c3474ae3ea66015c84054c0040532faf57ea8`. Core `700a69f659829592e082f07044986f81fb593c1b6c1121eea931b2004e481325`.

Los siete ficheros de clase usados en estas conclusiones son idénticos en IF47; véase `class-comparison.json`. IF47 añade diagnóstico de XP, no una corrección de XP.

## Resultado y límites

Ocho comprobaciones focalizadas pasan con las clases reales de IF46 bajo JDK25 y `-Xverify:all` (`EndItemActionsProbe.java`, `checks.txt`). Reutilizan la frontera de conexión/registro/transporte del harness de XP sin ejecutar su batería anterior. Ninguna prueba abre Minecraft, valida aceptación de Bedrock, completa una teletransportación, dispara contra un dragón o aterriza con élitros. Se prueban transacciones/modelos de datos, no se certifica el combate completo.

`item-mapping-audit.json` revisa uno por uno 49 identificadores de equipo/materiales y 63 filas de cuatro tablas oficiales de botín: cada ID del equipo tiene declaración de runtime, mapping y destino existente en el registro Java incluido. Esto es presencia de datos, **no** una prueba de funcionamiento. Los 16 colores de caja de shulker se comprueban individualmente; no significa que todos se generen naturalmente.

Las tablas de botín oficiales usan algunos nombres históricos (`map`, `horsearmoriron`, `horsearmorgold`, `horsearmordiamond`, `record_otherside`, `dye`). Pueden ser alias que el servidor resuelve antes de enviar el inventario; su ausencia literal del registro de red NO demuestra que el botín desaparezca. El informe comprueba también sus contrapartidas modernas. El `dye` de stronghold_crossing fija `data:0` (tinta negra histórica), pero no se ejecutó el conversor nativo del servidor. El botín moderno incluye lanza de diamante y armadura de caballo de cobre: ambos tienen mapping a ID Java existente.

## Lista por objeto/acción

| Objeto o acción | Comprobación realizada | Resultado y pendiente |
|---|---|---|
| Arco: iniciar tensado | `InteractionPackets.beginItemUse` con arco real de la clase, `StartUsingItem` | Pasa la prueba de traducción. La flecha que falta al tensar es un fallo visual comunicado; lo investiga el agente de render. No declarado resuelto aquí. |
| Arco: soltar/disparar | `InteractionPackets.handlePlayerAction`, líneas79–105 | Ruta `ReleaseItemTransaction`, slot/stack/posición nativos revisada. Falta disparo real/impacto en servidor. |
| Ballesta: iniciar carga | Mismo helper, ID crossbow | `StartUsingItem` pasa. La cadencia y aceptación requieren juego. |
| Ballesta: flecha cargada | `ItemDataRewriter.chargedProjectiles`, línea215 | Pasa para `chargedItem` arrow; se excluye carga vacía o no admitida. El conversor del proyectil anidado es una frontera explícita del test. |
| Ballesta: cohete cargado | Mismo helper con firework_rocket | Pasa la conservación de proyectil. No se valida daño/explosión. |
| Flechas normales y con efecto | Mapping `arrow` a IDs/componentes Java; ruta normal de uso | Datos declarados. Trayectoria, colisión, daño y recogida de flecha requieren servidor. |
| Tridente | Inicio de uso probado; liberación genérica revisada | Pasa inicio; lealtad/empalamiento/retorno y daño no ejecutados. |
| Espada, hacha, lanza | `ClientPlayerPackets` ATTACK, líneas730–756 | Traduce JID registrado a RID nativo. El dragón multipartes requiere la auditoría de entidades; no se certifica golpe solo porque existe ATTACK. |
| Escudo | `beginItemUse` y RELEASE reconocen estado de bloqueo | Se conserva corrección anterior. Se necesita daño real frontal/lateral para validarlo; no repetir pruebas históricas ni afirmar invulnerabilidad. |
| Perla de ender: lanzar | `beginItemUse` y cooldown | Pasa Use main-hand con stack/slot exactos y cooldown componente1s. No se inventa destino, daño ni endermita. |
| Perla: aterrizaje/teletransporte | Ruta de corrección de posición del jugador, tarea de mundo/root | No ejecutado en juego; el destino lo decide el servidor. |
| Ojo de ender: lanzar | Helper con ID nativo `minecraft:ender_eye` | Pasa Use main-hand. La búsqueda del stronghold/desgaste/rotura/destino sigue en servidor. |
| Ojo: insertar en marco | `sendUseItemOnBlock` líneas148–209 | Ruta genérica Place revisada; no filtro End. No se ejecutó inserción real ni activación de los12 marcos. |
| Cristal del End: colocar/reinvocar | Mismo Place y mapping específico | Datos presentes. Necesita prueba real de base, espacio y ritual de cuatro cristales. No se certifica resurrección. |
| Cohete: impulso con élitros | `beginItemUse` con jugador gliding | Pasa: una solicitud diferida, consumo de una copia, netId solo se quita de copia; inventario original intacto. No valida boost aceptado. |
| Cohete: uso desde suelo | Helper con jugador no gliding | Pasa ruta Use normal y no se encola boost. |
| Cohete: duración y explosiones | `FireworkData.fireworks` | Pasa Flight3 sin explosiones y caso sin NBT. No se simula vuelo físico. |
| Élitros: equipar/iniciar | Mappings presentes. `ClientPlayerPackets` START_FALL_FLYING, líneas637–640 | Activa gliding y StartGliding en puente. No se ejecutó equipamiento ni despegue real. |
| Élitros: levitación/suelo | `ClientPlayerPackets` líneas947–957 | StopGliding ante suelo o levitación revisado. Colisiones, aterrizaje y daño necesitan juego. |
| Fruta de chorus: comer | Helper con tag real is_food | Pasa StartUsingItem; tag incluido en datos. Fuente oficial declara32ticks, teletransporte8bloques, cooldown20ticks y comer con hambre llena. Teletransporte no probado. |
| Fruta de chorus cocinada | Mapping específico presente | Dato/recetas del servidor; no tiene acción de comer revisada. Cocción/crafteo no ejecutados. |
| Botella vacía: usar | Helper glass_bottle | Pasa Use main-hand; `FluidInteractionTarget` también rastrea agua. La recolección de aliento sobre nube real no está probada. |
| Aliento de dragón | Mapping dragon_breath presente | Inventario identificado; recolección/consumo en soporte de pociones requiere servidor. |
| Poción bebible | Helper y `ItemDataRewriter.potionContents` | Inicio probado; mappings de efectos declarados, incl slow_falling y levitation. Aplicación real/duración no ejecutadas. |
| Poción arrojadiza | Helper splash_potion | Pasa Use; impacto/efectos sobre mobs y jugador requieren servidor. |
| Poción persistente | Helper lingering_potion | Pasa Use; nube/radio/daño requiere auditoría entidades y juego. |
| Huevo de dragón | Mapping de bloque/objeto presente | Interacción genérica. Teletransporte, caída y recogida con pistón/antorcha no probados. |
| Cabeza de dragón | Mapping dedicado moderno presente | BlockEntity skull y animación, tarea mundo. No confundir el alias viejo skull con la cabeza moderna. |
| Caparazón de shulker | Mapping shulker_shell presente | La entrega del drop la decide servidor; crafteo de caja no ejecutado. |
| Caja shulker sin teñir y16colores | Cada ID/runtime/target revisado | Inventario de bloque se abre como shulker y se conserva NBT nativo; contenidos de objeto no tienen conversión explícita a componente Java CONTAINER en ItemDataRewriter: tooltip de contenido no certificado. No implica pérdida del contenido servidor. |
| Cofre y cofre de ender | `InventoryPackets` líneas1189–1217/1498 | UI genérica reconoce chest/shulker/ender_chest; transacciones confirmadas por servidor. No se ejecutó cofre de ciudad, inventario personal ni cofre doble real. |
| Herramientas y armaduras de botín | Cada ID de tablas revisado; `ItemDataRewriter.handleItem` Damage/ench | Preserva datos de daño y traduce encantamientos registrados; no certifica resistencia al daño/caída ni reparación por Mending. |
| Libros encantados de bibliotecas | `ItemDataRewriter` encantamientos + `WorkstationItemData` | Mappings/repaircost revisados. Se conserva corrección IF46 del coste de yunque; el botín real y su aceptación no ejecutados. |
| Plantillas Eye y Spire | Cada mapping/runtime/target revisado | Datos presentes. Aplicar trim en mesa no probado en esta revisión. |
| Armadura decorada | `WorkstationItemData.applyTrim` | Material y patrón se buscan en registros y crean TRIM26_3; render de armadura real no ejecutado. |
| Manzanas, pan y resto de botín | Filas individuales en item-mapping-audit.json | Datos de todos los IDs/alias revisados. La generación de cofres corresponde al servidor, no al addon. |
| Mapa vacío/lleno y brújula | empty_map/filled_map/compass mappings presentes; MapPackets | Mapas con fallo visual previo siguen pendientes. No declarar icono/imagen sostenida corregidos. En End no debe inferirse navegación normal del indicador de brújula. |
| Cubo de agua/cubo vacío | Mappings y correcciones IF44/45 conservadas | Agua sobre bloque End exige prueba real; no concluir waterlogging completo resuelto. |
| Tótem en mano secundaria | Mapping y evento TALISMAN_ACTIVATE→PROTECTED_FROM_DEATH en EntityPackets | Dato y evento revisados. Salvar muerte real/vacío no ejecutado; tótem no asegura salir del vacío. |
| Calabaza tallada | Mapping y ruta equipo nativos | El servidor controla provocación de enderman; no probado en juego. |
| XP y Mending | Ruta de atributos y diagnóstico IF47 ya auditadas | Fallo actual comunicado: orbes hasta20s sin recogerse; no corregido. IF47 solo genera traza; no se certifica XP del dragón ni reparación. |

## Observación que NO justifica cambio todavía

Eye y EndCrystal son objetos sin `blockRuntimeId`, y `PlantingPrediction.consumesItem` solo reconoce plantaciones. Su acción de inventario predicha en `sendUseItemOnBlock` conserva cantidad hasta actualización del servidor. Este hecho está en código, pero **no reproduce un rechazo** en Bedrock. La aceptación/consumo de insertar ojo y colocar cristal es una prueba prioritaria dentro del servidor; modificar consumos a ciegas podría perder/desincronizar inventario.

No se modificó ninguna clase de producción para esta auditoría.
