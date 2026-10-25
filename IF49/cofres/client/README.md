# IF49: reutilizar una vez la presentación nativa ya preparada

## Trabajo eliminado

El flujo nativo de cofres hace primero una comprobación completa con `screen.prepare(font,width,height)` antes de instalar la pantalla. Esa llamada ejecuta `refresh`: presenta todos los slots, resuelve el programa de UI, serializa el árbol, lo vuelve a parsear para el renderer y calcula layout, cobertura y alcance de acciones. El `init()` de la misma pantalla vuelve a cerrar/crear el renderer y ejecutar ese mismo trabajo.

La clase de IF48 es idéntica a IF34 y conserva esa ruta. La reproducción de control de este informe ejecuta los cuerpos reales originales de `prepare()` e `init()` y cuenta dos refresh. El cliente completo26.3 no está disponible aquí; no se afirma haber ejecutado su `setScreenAndShow`, sus mixins transformados o su renderer.

El perfil local utiliza los programas vanilla exportados por el core real IF48, no un menú simplificado: cofre27slots1382nodos/663561bytes y cofre54slots1949nodos/940942bytes. Con fronteras simples para presentación de items y medidas de fuente/imagen, la combinación `fromProgram + resolve + NativeUiScene.parse + lifecycle/layout/geometry` tuvo medianas calientes104.4ms y80.5ms respectivamente (8muestras tras4 de calentamiento). Es trabajo CPU local medido, no los FPS del ordenador del usuario; no incluye GPU, conversión NBT de items ni lectura PNG. `profile.txt` contiene las fases y límites.

## Cambio

Se reemplaza solo `com/viaversion/viafabricplus/bedrock/screen/container/BedrockNativeContainerScreen.class`. No hay nuevos archivos de ejecución, cambios de mixins o de red.

Después de que el preflight original termine con éxito, la pantalla guarda una marca de un solo uso, el Font, dimensiones y texto de título usados. Sus snapshots originales de slots, cursor, `showTags` e imágenes siguen siendo la referencia de contenido.

El primer `init()` sigue llamando a `super.init()` y poniendo `leftPos/topPos=0`. Solo reutiliza el renderer/escena ya validados si todos los datos siguen coincidiendo: propiedad actual del menú, renderer presente, sin activación pendiente/aceptada, mismo Font y dimensiones, mismo título, mismos slots y cursor por `ItemStack.matches`, mismo `showTags` y revisión de imágenes.

La marca y las referencias Font/título se consumen antes de evaluar los guards. Cualquier diferencia deja actuar al cuerpo original de cierre/creación/refresh. Un segundo init o un resize, incluso con el mismo tamaño, siempre ejecuta la ruta original. `prepare()` invalida la marca anterior al entrar, y solo rearma al terminar sin error. `discard()` invalida la marca y libera esas referencias antes de realizar la limpieza existente; `removed()` ya llama a `discard()`.

Las validaciones completas de cobertura y acciones del preflight se mantienen. Si la pantalla ya está activada o pendiente, el primer init también conserva la revalidación original. No se cambian el flujo de clicks, inventario, transferencias o presentación estable de frames.

Se mantiene el puente de texto de JSON: `ContainerUi` usa Gson sombreado de ViaVersion mientras `NativeUiScene` usa Gson normal. Sustituirlo por una sobrecarga directa necesitaría una conversión entre dos jerarquías de tipos, y no forma parte de este parche.

## Construcción y verificación

`PatchPreparedContainerIF49.java` se compila con Java25 y ASM; no usa clases falsas de Minecraft para compilar producción. Fija el SHA256 de entrada y conserva todos los frames antiguos. Solo necesita aumentar el `maxStack` de `discard` de1a2 para sus nuevas asignaciones; la reversión restaura también ese máximo.

- Todos los métodos de la clase completa original y candidata pasan ASM BasicVerifier.
- Al quitar los cinco campos, el helper y las adiciones de `prepare/init/discard`, se recupera canónicamente toda la clase original. Los demás métodos, bytecode, anotaciones y frames permanecen idénticos.
-18 grupos de comportamiento ejecutan el bytecode exacto de esos métodos en un host de prueba recortado. Cubren2→1refresh, one-shot, super/init/orígenes, reinicialización posterior, Font, ambas dimensiones, slots, count/componentes, cursor, `showTags`, imágenes, título mutable, renderer ausente, activación, propiedad de menú, rechazos de cobertura/scope, fallo de reprepare y cleanup.

El host copia esos métodos sin editar sus instrucciones. Reemplaza únicamente los límites pesados `refresh/current/showTags`, superclass y API de items/renderer/imágenes por fronteras explícitas que cuentan trabajo y permiten provocar cambios/fallos. No ejecuta toda la clase de pantalla contra un Minecraft real y no sustituye una prueba gráfica. Las clases de prueba no deben distribuirse.

La evidencia de ABI se detalla en `abi-evidence.md`. El código revisable equivalente está en `runtime-source.txt`; el `.class` productivo es el que fija `class-hashes.json`.

## Archivos que pueden archivarse como evidencia

Generador, documentación, JSON de hashes/verificación, los `.txt` de comprobaciones, el perfil y sus fuentes, `EmitPreparedContainerBoundaryHost.java` y los `.java` de `boundary-src`. No incluir clases compiladas de `boundary-classes`, `host-original`, `host-candidate`, `generator-classes` o el perfil. Tampoco es necesario copiar los árboles completos `*-resolved.json`, ni fuentes completas remotas de terceros.
