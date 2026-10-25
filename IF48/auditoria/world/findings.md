# End y stronghold: mundo, bloques y transmisión

Base examinada: core entregado IF46. Las nueve clases de mundo relevantes y el recurso de mapeos coinciden byte por byte con IF47 (`class-comparison.json`). Las clases que difieren de IF34 son `WorldPackets` y `ChunkTracker`: la comparación por método (`current-method-comparison.txt`) conserva 39 y 108 métodos originales respectivamente; en WorldPackets cambian dos manejadores de actualización de bloques. ChunkTracker conserva todos sus métodos originales y añade funciones de observación de capas. Las referencias de líneas siguientes son del decompilado almacenado y se acompañan de la comparación con las clases entregadas.

No se ejecutó un cliente gráfico de Minecraft ni un servidor Bedrock. «Ruta presente» o «prueba dirigida» no significan combate, generación, colisión o teletransporte validados dentro del juego.

## Hallazgo y corrección propuesta

`chorus_plant` solo tiene un estado nativo sin conexiones. El recurso lo proyecta a `minecraft:chorus_plant[down=false,east=false,north=false,south=false,up=false,west=false]`. `ConnectedBlockShapes.isConnected` excluye chorus y `shape` no reconstruye sus brazos. Dos pruebas sobre las clases entregadas confirman que aun con piedra del End debajo, tallo encima y flor al este, el resultado no cambia (`read-only-checks.txt`). Esto demuestra una carencia de la proyección de estados, sin necesidad de atribuir al servidor un fallo de crecimiento.

La candidata en `fix/` añade una reconstrucción exclusiva de chorus en la representación Java. 13 pruebas dirigidas pasan y la misma regresión falla con IF46. Ejecutan `ChunkTracker.getJavaBlockState`, `remapChunk` inicial, actualización de vecinos tras quitar una flor, reconstrucción entre chunks, escaneo de borde al cargar un chunk y búsqueda vertical entre secciones. Los 64 patrones de brazos producen estados registrados reales de Java. No cambia la paleta nativa, inventario, crecimiento, drops, gravedad, ACK ni paquetes al servidor. La revisión independiente añade 8 verificaciones, incluidos 120 controles diferenciales de rutas ajenas a chorus con resultados idénticos al original, coordenadas negativas, límites verticales del End y ausencia de cambios en la paleta nativa. Total: 21 pruebas dirigidas de la candidata; los 64 patrones/120 controles están agrupados y no se suman otra vez.

Contrato de representación: tallo o flor conectan en las seis direcciones; piedra del End conecta por abajo. Es la proyección canónica Java corroborada por la fuente histórica fijada en `reference/source-manifest.json`, y no debe confundirse con haber ejecutado el cliente 26.3. La implementación nativa de terceros PMMP conecta piedra del End en más direcciones; esa diferencia está documentada y no se presenta como comprobación de la geometría BDS.

## Bloques, estados y block entities, uno por uno

La tabla `BLOCKSTATES.md` / `mapping-audit.json` contiene 80 filas por identificador candidato: 74 identificadores/filas actuales con todos sus estados nativos presentes y destinos Java registrados; seis candidatos son nombres genéricos o antiguos sin identificador actual. No hay estados nativos sin mapeo en este conjunto. `mappings/MAPPINGS.md` (en el directorio hermano de auditoría) añade la comprobación más fuerte del rewriter real en IDs secuenciales y hashados. La presencia de un mapeo no basta, como demuestra chorus.

|Elemento|Ruta revisada y resultado acotado|Lo que sigue sin probar en juego|
|---|---|---|
|Piedra del End y ladrillos|`custom/blockstate_mappings.json`: `end_stone`, `end_bricks`; destinos sólidos registrados.|Minería, tiempo de rotura, drops y resistencia al dragón son del servidor.|
|Purpur bloque y pilar|Mapeos de todos los ejes presentes; pilar conserva eje en Java.|Orientación real al colocar y resistencia/destrucción.|
|Purpur losa/doble losa/escalera|Todos los estados mapeados; escaleras pasan por `ConnectedBlockShapes.shape`; controles de registro del candidato chorus.|Colocación, agua secundaria y colisión gráfica real.|
|Planta de chorus|Carencia confirmada, candidata y pruebas descritas arriba.|Forma renderizada y aceptación del servidor.|
|Flor de chorus|Seis edades mapeadas; utilizada como vecino en pruebas de seis direcciones.|Crecimiento aleatorio, supervivencia y caída de flores.|
|Obsidiana y bedrock|Mapeos completos presentes; no hay generación local de pilares/plataforma/fuente.|Estructura exacta, regeneración al entrar y al reaparecer el dragón.|
|Barrotes de jaulas|16 combinaciones de conexiones nativas mapeadas a Java.|Impactos de flechas y colisión entre barras.|
|Varas del End|Seis direcciones mapeadas; luminosidad proviene de tabla Java y motor `LightEngine`.|Iluminación renderizada y orientación real al colocar.|
|Cristal magenta|Identificador y destino presentes.|Transparencia con packs/shaders instalados.|
|Escalera y antorcha|Orientaciones nativas mapeadas, incluidos destinos de antorcha en pared.|Movimiento, soporte y rotura.|
|Cabeza del dragón|Seis estados nativos; `SkullBlockEntityRewriter.toJava` conserva orientación de pared y traduce `Rot`/`Rotation` para suelo.|Animación de mandíbula y alimentación por redstone.|
|Estandarte magenta|Estados `standing_banner`/`wall_banner`; `BannerBlockEntityRewriter.toJava` convierte Base y Patterns y selecciona bloque Java de color.|Textura y animación gráfica; no se probó render.|
|Cofre de ciudad/barco|`ChestBlockEntityRewriter` y contenedor correspondiente, apertura/cierre por `WorldEffectPackets` block event.|Loot generado y transacciones de todos los objetos.|
|Cofre de Ender|Mapeo, block entity NOOP apropiado y block event de tapa presentes.|Contenido vinculado al jugador y persistencia del servidor.|
|Soporte para pociones del barco|Ocho combinaciones de botellas y `BrewingStandBlockEntityRewriter` registrados.|Contenido exacto de las pociones generadas y fabricación.|
|Huevo del dragón|Mapeo y evento `ParticlesDragonEgg` presente.|Interacción que teletransporta, caída y recolección son del servidor.|
|Portal de salida|Estado Java `end_portal` y block entity NOOP; transición vía CHANGE_DIMENSION.|Entrada efectiva, destino/posición aceptada y créditos (revisión separada root).|
|Gateway del End|Estado y `EndGatewayBlockEntityRewriter`; cuatro pruebas verifican Age int→long, ExitPortal lista→intarray exacto, posición y opcionales ausentes. Block event1 está admitido por `WorldEffectPackets`.|Teletransporte real, enfriamiento y animación del rayo.|
|Fuego|16 edades mapeadas, daño traducido por ruta genérica.|Propagación y extinción del servidor.|
|Stronghold mampostería|Cada variante de piedra/ladrillos/musgoso/agrietado/cincelado e infestados aparece por separado en la tabla de estados.|Distribución y generación efectiva del stronghold.|
|Stronghold losas/escaleras|Cada identificador actual de piedra, piedra lisa y roble aparece en tabla; los nombres genéricos antiguos están señalados.|Peldaños, colisión y orientación efectiva.|
|Puertas de hierro/madera, botón/placas|Estados nativos completos. `ChunkTracker.connectDoor` combina facing/open entre mitades y bisagra.|Apertura, energía/redstone y colisión real del servidor.|
|Biblioteca: estanterías, telaraña|Mapeos presentes; telaraña proyecta a cobweb.|Ralentización real, minería y drops.|
|Generador de silverfish|Mapeo `mob_spawner`; `MobSpawnerBlockEntityRewriter.toJava` copia Delay/rangos y EntityIdentifier→SpawnData.entity.id.|No ejecuta spawn, AI ni ilumina/desactiva generador. Entidad silverfish revisada por apartado de criaturas.|
|Marco de portal del stronghold|8 estados nativos reconocidos por recurso/revisión del rewriter (4 orientaciones × ojo presente/ausente); orientación y ojo proyectados a Java.|Colocación del ojo, consumo, orientación de los 12 marcos y activación del portal son del servidor.|
|Agua/lava|16 niveles por familia actual y fluyente; dos capas nativas conservadas en `ChunkTracker.getJavaBlockState`/remapChunk. Agua secundaria activa waterlog cuando el destino Java lo admite.|Propagación y recogida del cubo dentro del End. IF44 tenía pruebas dirigidas previas, no se cuentan otra vez aquí.|
|Caja de shulker colocada|Mapeo; `ShulkerBoxBlockEntityRewriter.withFacing` aplicado desde `ChunkTracker.getJavaBlockState`.|Tapa, obstrucción, contenido y reapertura en servidor.|

## Mundo y mecánicas

|Mecánica|Comprobación|Límite|
|---|---|---|
|Identidad dimensional|Prueba real: Bedrock dimensión2 se resuelve `Dimension.END` y `minecraft:the_end`.|No atraviesa un portal.|
|Registro del End|NBT real: min_y 0, height 256, logical_height 256, coordinate_scale 1, skybox end, has_ender_dragon_fight=true. Registros modernos incluyen has_skylight=true y sky_light_factor 0; no interpretarlos usando reglas de versiones viejas.|Registro sin cliente gráfico no prueba cielo/niebla.|
|Entrada al End|`WorldPackets`141–215 maneja CHANGE_DIMENSION, nuevo ChunkTracker y EntityTracker, posición con eyeOffset, loading screen, RESPawn Java, salud/efectos/atributos/inventario.|Solo revisión estática de ruta; no prueba destino aceptado ni llegada segura a plataforma.|
|Confirmación de transición|`ClientPlayerPackets`456–465 maneja ChangeDimensionAck, responde servidor, cierra loading screen, emite posición/chunks-load y limpia dimensionChangeInfo.|No se ejecutó una transición completa.|
|Chunks del End|`WorldPackets` LEVEL_CHUNK filtra dimensión actual; `ChunkTracker` usa minY/height del registro y reescribe paleta. Prueba actual remapChunk del candidato chorus ejecutada con paletas controladas.|No hay servidor entregando islas/ciudades reales.|
|Isla central, externas, pilares, jaulas, plataforma, ciudades, barcos, fuente|Generadas por servidor, recibidas por el mismo pipeline de chunks. Se revisan todos sus bloques y entidades por separado.|No se verificó una semilla o terreno real ni que exista una estructura concreta.|
|Puentes/colocación rápida|Rutas de colocación conservan IF43/45; candidata chorus no las modifica (exact canonical reversal).|Todavía hace falta resultado del usuario sobre desaparición de bloques con IF45+; no afirmar resuelto para este servidor.|
|Agua y cubos para caídas/Enderman|Mapeo, capas y ray-target de fluidos existentes revisados; sin rama especial End que suprima agua.|Simulación de fluidos, caída y servidor pendientes.|
|Camas y ancla de reaparición|Se envía la interacción genérica de bloque (`InteractionPackets.sendUseItemOnBlock`153–204), con ubicación/cara/click/item nativo. Explosión/daño usan rutas genéricas.|No se ejecutó explosión ni se certifica seguridad de combate con cama.|
|Vacío, caída y muerte|Las alturas/dimensión se conservan; corrección de posición y daño/respawn siguen rutas genéricas.|Daño, pérdida/recuperación de objetos y posición final son del servidor, no probados aquí.|
|Teletransporte de perla/fruta/gateway|Movimientos autoritativos del servidor no dependen de crecimiento o generación local. Eventos/partículas de perla y huevo están mapeados.|No se ejecutó un uso real ni se probó aceptación del destino.|
|Explosiones, partículas y sonidos|WorldEffectPackets recibe eventos por dimensión; recursos contienen explosión, DragonBreath/Trail/Fire, DragonDestroyBlock y ParticlesDragonEgg.|Recursos presentes no prueban sincronía gráfica/sonora; revisión de criatura/efectos complementaria.|
|Tiempo, cielo, música y niebla|Registro/bioma End y WorldClockTracker presentes. Se conserva reloj End moderno fijo, atributo música End y cielo específico.|No se verificaron shaders, packs activos ni pantalla de créditos.|
|Crecimiento, redstone y dificultad|El addon no ejecuta generación o ticks de servidor; transmite cambios recibidos.|No convertir diferencias Java/Bedrock en errores del puente sin observar servidor.|

## Archivos de prueba

- `src/.../EndWorldAuditProbe.java`, `read-only-checks.txt`:8 verificaciones de diagnóstico; dos confirman la carencia chorus, seis validan conversión gateway/enum dimensional.
- `fix/src-test/.../ChorusConnectionsRegression.java`, `fix/baseline-IF46.txt`, `fix/patched-IF48.txt`:13 casos de regresión dirigidos, incluido remapeo inicial/vecinos/bordes con clases reales.
- `fix/PatchChorusConnectionsIF48.java`, `fix/patch-verification.txt`: dos prefijos condicionales, reversión exacta al bytecode canónico IF46 y BasicVerifier de todos los métodos de ambas clases.
- `audit_mapping_resources.py`, `mapping-audit.json`: segunda comprobación independiente de los recursos, no una simulación del servidor.
- `reference/source-manifest.json`: procedencia, hash y límites de las fuentes históricas/terceros de chorus.

No se incluyen medios, logs privados, mundos ni clases de fixtures dentro de ninguna clase de producción.
