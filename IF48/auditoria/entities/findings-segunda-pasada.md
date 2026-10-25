# Segunda pasada: renderer y selección de dragón/Enderman

**Se comprobó con clases reales que los recursos estándar entregados no producen un renderer nativo compatible para el dragón ni para el Enderman en los casos ensayados.** Por ello no debe descartarse la falta de PHASE/CREEPY alegando que siempre se verá un modelo Bedrock alternativo. La selección mediante HITBOX es una ruta independiente: necesita metadata entrante; no se genera automáticamente desde el modelo.

Esta pasada añade **13 afirmaciones** a las 58 de la primera: 4 de resolución/apariencia y 9 de gates, mensajes y estado puro de selección. No son pruebas de combate jugado. No se cambió producción. Se usaron el outer IF46 estable y su core actual. Las 17 clases outer nuevas inspeccionadas son byte a byte iguales a IF34; los hashes están en `verification-segunda-pasada.json`. No se compiló, descargó ni sustituyó ninguna clase Minecraft.

## 1. Ruta activa y configuración predeterminada

No hay una opción de ajustes que obligue a usar el renderer nativo para todos los mobs. La configuración ViaBedrock trae `translate-resource-packs: true` y su getter usa true por defecto (`ViaBedrockConfig.java:32`). Si esa opción sigue activa y la versión cliente es la objetivo, ResourcePackPackets prepara y anuncia el pack convertido. El proveedor del addon carga la biblioteca Bedrock incluida en el mod aun cuando el servidor no solicita packs externos. ResourcePackStorage añade esos packs builtin al stack inferior; NativeEffectsResourceRewriter codifica ese stack en `assets/viabedrock/actor_library.bin`.

En el cliente, `BedrockAcceptedResources.read` solo lee ese archivo desde un recurso cuya procedencia Java sea **PackSource.SERVER**. Se refiere al pack convertido y aceptado por el cliente, no a que cada layer interna tenga `server=true`. Si el recurso no está aceptado/disponible, devuelve lista vacía y `BedrockActorResources.get` devuelve null. El builtin incluido no habilita por sí solo cualquier entidad sin ese paso.

El mixin de EntityRenderDispatcher intenta `BedrockWorldActorRenderer.prepare` al retornar el renderer original. No modifica el tipo de entidad Java ni evita que EnderDragon tenga partes Java. La sustitución solo se aplica si prepare retorna true.

| Situación inspeccionada | Decisión del código |
|---|---|
| No existe library aceptada o snapshot nativo | Conserva renderer original. |
| Entidad muerta, invisible o Avatar | Conserva renderer original. |
| LivingEntity pasajera, durmiendo o con algún slot de equipo no vacío | Conserva renderer original. |
| Recursos por entidad aún no resueltos, inexistentes o fallidos | Conserva renderer original. La resolución es asíncrona. |
| Constructor de apariencia lanza IOException/RuntimeException | Cachea Optional.empty y conserva renderer original. |
| Grafo tiene efectos, falta alguna query, superficies/modelo/material/imagen no compatibles | sample retorna null o lanza excepción; marca actor.failed y conserva renderer original. |
| Todos los pasos producen frame y textura | Sustituye por el renderer Bedrock para ese frame. No sustituye lógica de entity picking ni el tracker. |

El registro de canales nativos es automático en `ViaFabricPlusBedrock.onPreSettingsLoading` mediante Fabric INIT. El core solo publica estado/picking después de `markNativeActorsJoined`, que llaman JoinPackets y el cambio de dimensión. No se verificó que la sesión del usuario haya cargado/aplicado esos mixins o aceptado ese pack: aquí se rastrean sus decisiones y se ejercen sus componentes puros.

## 2. Ensayo real de los recursos estándar entregados

`AppearanceFallbackProbe.java` carga **BedrockBuiltinAssets.load** del propio JAR IF46, resuelve `BedrockEntityResources.merge` con la biblioteca completa y sin overrides del servidor e instancia **BedrockWorldActorAppearance** real. No se reproduce su implementación en una clase falsa. La prueba usa `-Xverify:all` y modo AWT headless; no llama a Minecraft, a un renderer gráfico ni a un servidor.

### Dragón

- La biblioteca resuelve la definición de `minecraft:ender_dragon`.
- El constructor real de apariencia falla con `IOException: Invalid actor animation script`.
- La definición efectiva incorporada de vanilla_1.21.70 contiene **176** líneas pre_animation, de **23 434** caracteres concatenados. `BedrockMolang.Program.script` admite 256 líneas pero limita la concatenación a **16 384** caracteres. La cantidad de líneas cabe; la longitud excede el límite. La excepción se produce antes de ejecutar la animación.
- El código real de Renderer.prepare atrapa precisamente IOException/RuntimeException al construir esa apariencia y conserva el renderer original. **Elevar solo el límite no es una solución validada**: las expresiones del dragón también requieren historial/estado que no están en el conjunto de queries del renderer.

Esto prueba una causa reproducible de fallback con los recursos estándar del mod. Un pack externo que reemplace la definición por otra más simple puede seguir otra ruta; no se afirma que todos los packs posibles fallen.

### Enderman

- La biblioteca resuelve `minecraft:enderman`; el constructor funciona y `graph.hasEffects()` es false.
- Se suministró el conjunto de queries de BedrockActorFrame y las que añade Renderer.prepare para un LivingEntity ordinario sin equipo, sin pasajeros y no durmiendo. Para los callbacks se usaron valores neutros controlados; no se simuló una entidad Minecraft.
- La muestra real retorna **frame=null**, sin excepción. El estado real de Molang registra estas queries ausentes: `is_crawling`, `is_angry`, `is_carrying_block`, `is_invisible`, `is_gliding`, `is_swimming`.
- `BedrockWorldActorAppearance.sample` exige `animation.hasCompleteQueries()`. El conjunto anterior fuerza null. Renderer.prepare conserva el renderer original y marca actor.failed.

El Snapshot nativo sí conserva ActorFlags como ANGRY, pero **BedrockActorFrame no los publica como is_angry**. No basta con que el flag exista en el core o en el mensaje para que el modelo nativo lo use.

La primera pasada comprobó que el conversor tampoco genera CREEPY/STARED_AT Java al recibir ANGRY. Ambas observaciones juntas delimitan un hueco visual real de la ruta estándar; no demuestran qué daño/agresión determina Bedrock ni qué aspecto vio el usuario en una sesión concreta.

## 3. HITBOX: de dónde llega y cuándo se usa

El único origen encontrado de las cajas es **ActorDataIds.HITBOX (118)** en la metadata recibida del servidor:

1. ADD_ENTITY lee ENTITY_DATA_ARRAY y lo aplica a la entidad; SET_ENTITY_DATA hace lo mismo por RID.
2. Entity.updateEntityData conserva el dato en su mapa con las validaciones del schema real.
3. `Entity.pickingHitboxes()` llama exclusivamente a `NativeActorHitboxes.read(entityData.get(HITBOX))`.
4. Ese lector exige tipo TAG/CompoundTag, una lista `Hitboxes`, vectores Min/Max/Pivot válidos y los límites correspondientes. Sin dato o con formato inválido devuelve lista vacía.
5. EntityTracker.entitySpawned crea la lista de proxies; los cambios de metadata/posición la actualizan.
6. Se emiten entidades Java INTERACTION y aliases de sus Java IDs hacia el UID dueño; para el addon se publica NativeActorPickingMessage con UUID del dueño, lifetime y UUID/dimensiones de cada proxy.

**No se encontró generador que deduzca las cajas a partir de un mob llamado ender_dragon, de RESERVED_053/054 (bounding dimensions) ni de las geometrías de recursos.** Las geometrías del modelo visual y la lista HITBOX de red son fuentes diferentes. Los schema públicos de AddActor/SetActorData admiten una lista de datos; no establecen que el ID118 deba estar presente en cada dragón. No hay captura de paquetes del backend del usuario que permita afirmar presencia permanente de HITBOX.

El tag 118 existe en referencias públicas de protocolo. Eso prueba que el campo está definido, no que BDS lo emita siempre ni que lo actualice al ritmo necesario en ese combate. Las implementaciones públicas alternativas y las muestras de comportamiento tampoco establecen la emisión del motor cerrado BDS.

### Nueve afirmaciones nuevas sobre gates y estado

`NativePickingAvailabilityProbe.java` ejecuta EntityTracker, Entity, NativeActorPickingMessage y **BedrockActorPickingState** originales:

| Caso ensayado | Resultado observado |
|---|---|
| Dragón sin HITBOX entrante | pickingHitboxes está vacío; no se inventan partes. |
| Canales state_v3+picking registrados, antes de join | No se publica picking. |
| Mismos canales después de join, sin HITBOX | Se publica Snapshot de dragón con parts vacías. |
| Estado cliente puro recibe Snapshot vacío con lifetime válido | Lo acepta, conserva elegible el dueño para selección vanilla. No se suprime el dragón por una lista vacía. |
| Solo canal state_v3, sin picking | No se publica mensaje picking. |
| Se añade un HITBOX válido controlado | Se publica un proxy y el estado cliente lo acepta. |
| Snapshot no vacío | Suprime el dueño de candidatos ordinarios y habilita su proxy usando la elegibilidad del dueño. |
| UUID de una parte desconocida | No se remapea ni suprime automáticamente como parte del dragón. Se delega a la elegibilidad ordinaria. |
| Retirada con lifetime coincidente | Limpia dueño y proxy en el estado cliente. |

Esas pruebas no ejecutan raycast de Minecraft ni la asignación de IDs de EnderDragonPart. Son suficientes para descartar la suposición de que «tener el addon instalado» fuerza siempre cajas nativas.

## 4. Alcance del riesgo de IDs y fases

Hechos verificados:

- El core crea un mob Java ENDER_DRAGON real.
- El contador asigna **un solo ID** al dragón y el siguiente al actor/proxy siguiente.
- El lookup Java ID solo entiende actores registrados y aliases de proxies; no hay registro específico de EnderDragonPart.
- El handler ATTACK cancela el ID si no encuentra dueño; si el ID pertenece a otro actor lo traduce al RID de ese actor.
- El cliente exacto 26.3 es multipart según el patch NeoForge fijado por commit. El renderer alternativo no elimina ni reemplaza su entidad lógica.
- Los recursos estándar del dragón usan el fallback Java por la excepción reproducida; el campo Java PHASE existe pero el conversor actual no lo publica.

Conclusión condicionada: **si** el cliente asigna partes en el rango que el core continúa asignando a otros actores/proxies, pueden existir selección cancelada, destino incorrecto o colisión de identidades. El riesgo sigue siendo prioritario aun con HITBOX nativos, porque éstos también consumen IDs y no se demostró supresión de todas las partes vanilla. La cantidad, base y manejo interno exactos de esas partes en 26.3 **no se verificaron**, y no se afirma un fallo universal de todos los ataques.

El PHASE ausente es relevante para el fallback estándar: la animación de Java no recibe una proyección explícita del aterrizaje/posado/muerte nativos. Su efecto concreto sobre hitboxes, blancos y combate requiere cliente exacto o una traza/prueba real. No se traduce SITTING a un número de fase aproximado ni se supone que todas las fases Bedrock coincidan con Java.

No se propone como arreglo elevar límites de scripts, forzar el renderer nativo, añadir hitboxes geométricas estimadas, duplicar ataques ni reservar una cantidad histórica de IDs sin verificar el contrato del cliente actual.

## Archivos y reproducción

- `appearance-output.txt`: 4 afirmaciones de biblioteca/apariencia y causas observadas.
- `picking-availability-output.txt`: 9 afirmaciones de gates/estado.
- `verification-segunda-pasada.json`: hashes de 17 clases outer, recursos seleccionados y pruebas; no producción modificada.
- `bundled/`: copias de referencia extraídas del propio IF46, sin archivos del usuario. No es necesario redistribuir todas para el informe público.

Las clases de pruebas se compilan contra el outer IF46 antes de cualquier copia histórica, más el core IF46 y dependencias guardadas. El classpath original `entities/classpath.txt` incluye el harness de transporte reutilizado.

```sh
javac -cp "/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF46.jar:$(cat classpath.txt)" -d test-classes AppearanceFallbackProbe.java
java -Xmx1800m -Xverify:all -Djava.awt.headless=true -cp "test-classes:/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF46.jar:$(cat classpath.txt)" com.viaversion.viafabricplus.bedrock.appearance.AppearanceFallbackProbe
javac -cp "test-classes:/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF46.jar:$(cat classpath.txt)" -d test-classes NativePickingAvailabilityProbe.java
java -Xverify:all -cp "test-classes:/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF46.jar:$(cat classpath.txt)" NativePickingAvailabilityProbe
```
