# ViaFabricPlus Bedrock IF42

[Descargar el mod completo IF42](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF42/ViaFabricPlus-Bedrock-IF42.jar)

IF42 corrige la búsqueda y selección de materiales que podían fallar porque Litematica y MaLiLib comparaban también los datos adicionales del puente Bedrock. Cubre las tres búsquedas de inventario de Litematica y los dos comparadores estrictos de MaLiLib. Conserva todas las correcciones de IF41.

1. Cierra Minecraft.
2. Descarga `ViaFabricPlus-Bedrock-IF42.jar`.
3. Mueve el addon Bedrock anterior fuera de `mods` y guárdalo como respaldo.
4. Pon IF42 en `mods`. Conserva **ViaFabricPlus base** y una sola versión del addon Bedrock.
5. Reinicia y comprueba en Mods que aparece **ViaFabricPlus Bedrock - IF42**, versión `InteractionFix42.1`.
6. Entra en tu servidor y prueba seleccionar un material de la construcción que ya tengas en el inventario, tanto en la barra como en las otras filas.

La entrega contiene el mod completo y no necesita un actualizador ni versiones intermedias. Requiere Minecraft Java 26.3 y Java 25 o superior. La integración se revisó contra Litematica 0.29.1 y MaLiLib 0.30.2; ambos siguen siendo opcionales.

## Alcance de la corrección

Se comparan copias de los objetos vanilla cuya identidad nativa se ha comprobado contra los registros y mappings de la conexión. Solo esas copias omiten las cinco claves privadas conocidas del puente y restauran el efecto de uso Java por defecto cuando el valor coincide con el generado por el puente o con ese valor por defecto. Los objetos originales, los slots y los datos enviados a Bedrock se conservan.

Los nombres, encantamientos, daño, componentes restantes y datos ajenos al puente siguen contando en la comparación. Los objetos personalizados, las restricciones y los datos nativos especiales o ambiguos mantienen la comparación estricta. Se admiten identificadores nativos negativos válidos y variantes verificadas mediante los mappings actuales. No se cambia la igualdad global de objetos ni los hashes de ItemType.

Los recuentos normales de la lista de materiales de Litematica 0.29.1 ya ignoran componentes; esta corrección cubre el reconocimiento para buscar y seleccionar materiales, no demuestra una reparación de esos recuentos.

## Comprobaciones y límites

`VERIFICACION.json` recoge la identidad del JAR, las clases añadidas, los hashes y la conservación del contenido anterior. Las fuentes y las comprobaciones seleccionadas están en `META-INF/stackanvil-if42-changes.zip`.

Pasaron 54 comprobaciones de política nativa, nueve de efectos y límites, ocho del adaptador y 17 comprobaciones estáticas independientes de ABI y anotaciones. La última ejecución del adaptador usó exactamente los hashes finales del cliente y de la política. Las comprobaciones de versiones anteriores se conservan como evidencia y no se repitieron.

Las comprobaciones locales de la política usan las clases reales de ViaBedrock, sus mappings y NBT. Las pruebas del adaptador usan límites simulados de Minecraft explícitos: verifican copias, componentes y búsqueda, pero no equivalen a iniciar el juego. La inspección ASM comprueba bytecode, descriptores y anotaciones; no se ejecutó el motor Mixin con los binarios reales de Minecraft y los mods opcionales. La reflexión del efecto de uso falla de forma conservadora si el constructor esperado no está disponible. No se inició un cliente gráfico ni un servidor real. Falta confirmar el resultado en el servidor del usuario.

El ghast conserva el parche de IF41; esta entrega no añade una nueva corrección de vuelo. La orientación de observadores, la recogida de experiencia y el mapa al sostenerlo siguen pendientes. No se incluyen vídeos, logs ni el mundo del usuario.
