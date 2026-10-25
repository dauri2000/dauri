# IF48: arco completo por el renderer Java de primera persona

El parche reemplaza una sola clase existente: `BedrockFirstPersonItemRendering.class`. Añade al inicio de `usesJavaRenderer` dos salidas: arco en mano principal y arco en mano secundaria. El resto de la clase se conserva al revertir canónicamente ese prefijo; `animationFrame` no cambia. No añade clases de ejecución ni modifica mixins, paquetes, munición, daño o disparos.

La selección nativa actual entrega `get_animation_frame=0` para BOW, mientras su controller incluido elige `default` con índice0 y `bow_pulling_0..2` con índices1..3. La pose del brazo avanza por otra ruta. Para los arcos con brillo, la extracción del attachable ya cae al item Java y puede quedar combinada con el actor nativo. La captura no permite demostrar cuál de ambas rutas estaba activa.

La decisión compartida se consulta antes de producir el frame nativo, antes del lifecycle nativo y antes de ofrecer la apariencia de primera persona. Elegir Java desde que el arco está en cualquiera de las dos manos evita mezclar esos caminos. Cambia la presentación de manos/arco mientras está equipado. El modelo Java26.3 selecciona las texturas de tensado mediante sus propias propiedades `using_item` y `use_duration`.

## Construcción y ABI

`PatchBowFirstPersonIF48.java` se compila únicamente contra ASM y la biblioteca estándar Java25. Lee la clase del JAR IF46 por SHA256 fijado y emite directamente el prefijo. La producción nunca se compiló contra clases falsas de Minecraft.

La llamada `ItemStack.is(Object): boolean` y las firmas existentes se conservan literalmente del método distribuido. El nuevo campo `Items.BOW` se contrastó con NeoForge rama26.3 en el commit fijo `090aba227fefe4bf0f5eaf5834c68851498c1040`: `NeoForgeItemTagsProvider.java` líneas334 y349 usa `Items.BOW` en tags de items; `gradle.properties` declara `minecraft_version=26.3`. Las fuentes están en el directorio superior. Esto es evidencia de fuente compilable, no enlazado contra un cliente26.3 ejecutado aquí.

Comandos:

```bash
TASK_JAVA=/workspace/minecraft/tools/jdk-25.0.5.0.2/bin
TASK_ASM=$(cat /workspace/minecraft/analysis/asm-classpath.txt)
$TASK_JAVA/javac -cp "$TASK_ASM" -d tool-classes PatchBowFirstPersonIF48.java
$TASK_JAVA/java -Xverify:all -cp "tool-classes:$TASK_ASM" PatchBowFirstPersonIF48 /workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF46.jar classes
$TASK_JAVA/javac -d boundary-classes $(find boundary-src -name '*.java')
$TASK_JAVA/java -Xverify:all -cp boundary-classes BowFirstPersonDecisionProbe input classes
```

`input` contiene la clase original extraída para la prueba. El único archivo runtime autorizado es el `.class` que enumera `class-hashes.json`. No distribuir `input`, `boundary-classes`, `tool-classes` ni ninguno de los `.class` de pruebas. El código fuente de las pruebas es evidencia y puede archivarse como texto en el ZIP de cambios, no como clases cargables.

## Verificación

- Todas las funciones originales y candidatas pasan ASM BasicVerifier.
- Al quitar exactamente el prefijo nuevo se recupera toda la clase original canónicamente, incluidos sus métodos y atributos.
-13 comprobaciones dirigidas ejecutan los métodos reales originales y candidatos con una frontera mínima de API de items. Incluyen arcos en ambas manos, reposo/tensado, bandera de brillo, mapas, ballestas y matrices de decisión216casos. El brillo es una propiedad de la frontera; la política real no lo consulta y acepta todos los arcos.
- La función original/candidata de frames conserva resultados para arco, otros objetos y ballesta vacía/con flecha/con cohete. Las pilas de entrada permanecen intactas.

La prueba no ejecuta el renderer real, no emula el motor de Minecraft y no demuestra por sí sola que la imagen en una partida ya esté corregida. No se hicieron cambios sobre XP, ni se afirma que este parche resuelva el fallo de recogida de experiencia.
