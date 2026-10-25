# Revisión independiente del resolver IF49

La candidata es apropiada para incluirse con el alcance revisado. Reemplaza solo las dos copias superficiales de variables en `Session.expand`, y activa el mapa por capas únicamente cuando `UiBindings` es la clase exacta final `ContainerUi$Controller`. No guarda resultados evaluados para reutilizarlos en otra sesión o menú.

Se leyeron el helper completo, los usos de `values/controls` en el resolver y el generador. Una capa mutable nunca modifica su `base` ni una capa padre congelada; `snapshot()` copia el delta y las escrituras posteriores invalidan solo el snapshot de ese mapa. `clear()` suelta sus referencias sin alterar a los hermanos. Los JsonElements son referencias superficiales, como en las copias HashMap originales.

Los mapas pertenecen a una sesión `JsonUiProgram$Session`. El programa no mantiene una referencia a esas sesiones; el helper no añade mapas globales. La cadena de ámbitos queda dentro de las restricciones originales de profundidad/nodos del resolver. Las demás implementaciones de `UiBindings` conservan `HashMap` normal.

## Comprobaciones propias

Se ejecutaron siete grupos nuevos contra los bytes congelados y contra IF48 original, en JVMs separadas con `-Xverify:all`:

1. `Session.expand` real: una declaración local de un hijo no cambia a su hermano, el nieto conserva su ámbito y un binding de vista lee el snapshot del control fuente correcto.
2. `controls` real: el snapshot raíz no adquiere `$__effective_visible` escrito después; un panel oculto guarda el valor efectivo heredado anterior a su propia escritura de visibilidad y no expande sus hijos ocultos. Se verifica también el tipo efectivo de mapa usado por el pipeline.
3. Una sesión nueva sobre el mismo programa y controlador lee un binding cambiado A→B; la escena anterior permanece en A.
4. Una implementación `UiBindings` no nula y ajena al contenedor produce exactamente `HashMap` en los controles de una sesión real. Tanto esa implementación como bindings null usan el fallback HashMap de la fábrica.
5. Eliminar mediante la vista de claves del padre, reemplazar valores y hacer clear no cambia sus snapshots hermanos. La mutación de un JsonObject compartido sí se observa, preservando la copia superficial original.
6. Clave null/valor null con vistas respaldadas, computeIfAbsent, Entry.setValue y borrado de entradas; los snapshots anteriores conservan su contenido.
7. El `Session.expand` real acepta una hoja a profundidad64 y rechaza profundidad65 y el nodo8193 con los mismos errores que el original.

Las tres escenas propias y los errores de límites son idénticos byte por byte entre original y candidata; hashes en `output-comparison.json`. No se volvió a ejecutar ni contar la batería de32escenas o6000operaciones del autor.

Dos grupos estructurales propios adicionales:

- Al revertir exactamente los dos nuevos factory calls, se recupera canónicamente la clase completa Session original: todos los demás métodos, instrucciones, campos y atributos coinciden.
- Todos los métodos de las cinco clases candidatas pasan ASM BasicVerifier.

Total independiente: **9 grupos** (7 de comportamiento y2 estructurales), contados una sola vez aunque se ejecuten ambas variantes. Los cinco hashes se volvieron a contrastar con `candidate-class-hashes.json` y se fijan en `verification.json`.

La prueba usa el Session, controlador y helper reales, con JSON y presentaciones sintéticas explícitas. No ejecuta gráficos, cliente Minecraft completo ni BDS, y no mide los FPS del usuario. El perfil de rendimiento del autor es evidencia distinta y no se repite aquí.

No se modificó la candidata, reempaquetó el JAR ni publicó ningún archivo desde esta revisión. Para el ZIP de evidencia pueden incluirse estas fuentes, informes y `.txt/.json` de resultados. Las clases compiladas de `classes` son herramientas de prueba y no deben cargarse en el mod.
