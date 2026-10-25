# Segunda revisión de portales y stronghold

Se leyó el flujo del core IF46 y se contrastó su clase con IF47. Se revisó el cambio de dimensión completo en `WorldPackets.register`, la confirmación `ChangeDimensionAck` de `ClientPlayerPackets.register`, el registro `Dimension` y los datos de los marcos. Es lectura de rutas reales; no se ejecutó un trayecto dentro de Minecraft.

| Elemento | Evidencia y resultado | Límite |
|---|---|---|
| Ojo arrojado para localizar el stronghold | `UseItemPolicy` y entidad `eye_of_ender_signal` tienen traducción; el probe de objetos recorre Use. | Dirección, rotura y recuperación dependen del servidor. |
| Inserción de ojos | Se conserva `sendUseItemOnBlock`; el mapping cubre los cuatro facing y eye true/false. | No se ejecutó la aceptación de los doce ojos ni la creación del portal. |
| Geometría de los marcos | `MixinEndPortalFrameBlock` selecciona outline Bedrock 13/16 y shape de oclusión vacío del ojo. | No se cargó el motor Mixin ni se comprobó la selección en pantalla. |
| Portal completo | `end_portal` llega por datos de bloque; el puente no reconstruye el patrón con reglas Java. | Activación y orientación válida las decide Bedrock. |
| Cambio al End | `CHANGE_DIMENSION` interpreta dimensión 2 como `minecraft:the_end`; recrea ChunkTracker y EntityTracker conservando jugador, actualiza posición recibida, envía RESPawn Java y posición y vuelve a enviar inventario/atributos. | No se ejecutó ese handler con un servidor real ni se verificó la plataforma de llegada. |
| Confirmación de transición | `PLAYER_ACTION(ChangeDimensionAck)` contesta a Bedrock, termina la pantalla de carga, actualiza posición y borra `dimensionChangeInfo`. | Orden y entrega de paquetes en una conexión real pendientes. |
| Altura y chunks del End | `ChunkTracker` toma min_y/height y has_skylight del registro de dimensión recibido/proyectado. No impone altura del Overworld al End. | Carga de islas y distancias reales no ejecutadas. |
| Portal de retorno y créditos | Falta de SHOW_CREDITS detectada y corregida en IF48. 15 controles del helper/handler y 5 independientes. | Pantalla real y teleport final pendientes; lo determina el servidor. |
| Gateway | `EndGatewayBlockEntityRewriter` convierte Age int→long y ExitPortal list→int array; probe real comprueba valores. | Destino y traslado no ejecutados. |
| Generación de salas/ciudades | Chunks recibidos permanecen autoritativos; no se aplica un generador Java. | No se inspeccionó el mundo del servidor del usuario. |
| Cofres de las tres salas | Se revisaron cada entrada de las tablas oficiales y sus aliases de item. | Distribución, probabilidades, transferencias y contenido NBT reales pendientes. |
| Lepismas/generador/infestación | Mapping, actor y campos del generador tienen ruta; el catálogo contiene las variantes infestadas. | IA, nacimiento, infestación, daño y drops se ejecutan en el servidor. |

El registro de bloques y objetos pasa comprobaciones de datos. Esto no convierte las comprobaciones de portales, combate o generación en pruebas dentro del juego.
