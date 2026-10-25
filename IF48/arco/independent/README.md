# Revisión independiente del arco IF48

Candidata congelada: `com/viaversion/viafabricplus/bedrock/render/BedrockFirstPersonItemRendering.class`, SHA256 `912d2f1fe20fa1eeb7aceef04e01350c24349754cdd27ba195b1a346f2527215`.

## Resultado

Cinco comprobaciones independientes de estructura/rutas pasan; no se ejecutó el generador del propietario ni se reconstruyó la candidata. `IndependentBowScopeCheck.java` verifica:

1. El prefijo real solo comprueba arco en main/off y devuelve true. Su retirada recupera canónicamente **toda** la clase original IF46, no únicamente los métodos visibles.
2. BasicVerifier de ASM valida todos los métodos originales y candidatos.
3. La clase real `BedrockPlayerSkins` del JAR IF46 consulta la política exactamente en `firstPersonFrame`, `hasNativeFirstPersonAppearance` y `firstPersonAppearance`; no hay uso de esta política en inventario ni tercera persona.
4. Los bytes IF46 de PlayerSkins y ambos mixins de lifecycle/visibilidad coinciden con las fuentes IF34 leídas para analizar sus rutas.
5. `BedrockPlayerAttachables` sí difiere de IF34 por la corrección previa de escudo. Se descompiló separadamente la clase **real IF46** y se confirmó el uso de animationFrame y los filtros de brillo/escudo actuales. No se supuso que toda esa clase seguía idéntica.

Estas son comprobaciones nuevas de revisión, no otras cinco pruebas de gráficos. El informe del propietario contiene sus13 comprobaciones del método real con una frontera de API de items; no se vuelven a contar aquí.

## Rutas revisadas

- `BedrockPlayerSkins.firstPersonFrame`: si `usesJavaRenderer(main,off,active)` devuelve true, termina con null y no crea el actor/frame/attachables nativos. El wrapper `submitFirstPerson` conserva/restaura el frame en finally. Con frame null, `firstPersonItemSubmitted` y `firstPersonActorSubmitted` devuelven false y el mixin de visibilidad deja pasar el item y la mano Java.
- `hasNativeFirstPersonAppearance`: devuelve false antes de seleccionar la skin cuando la política elige Java. `MixinBedrockHandItemLifecycle.tickNativeHands` llama entonces al tick original, de modo que también quedan bajo Java las transiciones de item y altura al equipar, tensar y soltar; no cambia solo una operación de dibujado al final.
- `firstPersonAppearance`: devuelve null cuando el jugador actual tiene arco en una mano. Esto desactiva las rutas de bobbing/proyección nativas asociadas a esa apariencia. El render frame también evalúa la política sobre los stacks de su estado de dibujo; no basta con cambiar únicamente el uso activo.
- La condición nueva cubre el arco equipado antes, durante y después del uso, y en ambas manos. No consulta brillo, por lo que también cubre el arco encantado. Un arco en mano secundaria sigue sujeto a las restricciones de interacción Bedrock: el parche solo decide presentación.
- El `animationFrame` anterior sigue devolviendo0 para arcos; al elegir el renderer Java se evita esa ruta en primera persona. No se ha corregido ni probado su animación en tercera persona.
- El controller Bedrock incluido indexa default/pulling0/pulling1/pulling2 con `query.get_animation_frame`, mientras el método anterior produce0 para BOW. Es un defecto demostrado de esa ruta de modelo. Para items con brillo, el extractor de attachables omite el item pero el actor nativo podía seguir activo: la captura por sí sola no permite decidir cuál de esas dos rutas se veía.
- El modelo Java26.3 incluido en la referencia selecciona reposo/tensado mediante `minecraft:using_item` y `minecraft:use_duration`, con umbrales0.65/0.9. Esto respalda que delegar el arco normal completo a Java use sus texturas de tensado. No es ejecución del renderer26.3.

## Modelo de item y alcance

`CustomAttachableResourceRewriter.applyItemModel` solo asigna el modelo convertido a attachables definidos por packs del servidor (`isServerDefined`, pack ya cargado y geometría convertida). El arco vanilla no recibe ese override y conserva `minecraft:bow` de sus componentes Java. Un servidor que sustituya expresamente el modelo del arco es una variante distinta que esta revisión no valida. No se propone borrar indiscriminadamente modelos personalizados.

## Límites

No se cargó el cliente Minecraft26.3 real, su motor Mixin, gráficos ni un servidor Bedrock. La revisión valida cambio mínimo, ABI ya usada por el método, consistencia de selección y rutas actuales; no demuestra el resultado visual dentro de la partida del usuario. No se modifica munición, daño, paquetes de disparo, inventario o XP. XP y mapas anteriores continúan sin una corrección afirmada aquí.
