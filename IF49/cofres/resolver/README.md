# Resolución de cofres: snapshots de variables por sesión

La medición de las clases reales IF48 reproduce trabajo costoso al resolver la UI nativa del cofre. Un cofre grande genera 1.949 nodos y un JSON de escena de 940.942 caracteres. El perfil JFR atribuye la mayor parte de las muestras y asignaciones a copiar HashMaps de variables heredadas, dos veces por nodo. El parser de expresiones y el aplanado de plantillas aparecen como costes mucho menores: no se añade caché de expresiones ni de plantillas.

## Cambio aislado aprobado para IF49

Se sustituyen exactamente dos constructores `new HashMap<>(source)` de `JsonUiProgram$Session.expand`: la copia del ámbito heredado y la copia guardada en `controls`. El helper captura una instantánea superficial de un pequeño delta y referencias a capas ya inmutables. Las posteriores escrituras del padre no cambian las instantáneas previas; los propios JsonElements conservan el comportamiento superficial del HashMap original. No se almacenan resultados de expresiones ni valores evaluados de bindings para reutilizarlos entre frames o menús.

El helper solo se activa para la clase interna final exacta `ContainerUi$Controller`. Cualquier otra implementación UiBindings, incluidas otras pantallas/formularios/HUD, obtiene una copia HashMap normal. Los ocho tipos del controlador son cofres, ender chest, shulker, barril, hopper, dispenser y dropper. No incluye crafteo/yunque/trades ni cambia recetas, acciones de inventario, paquetes, slot IDs, cantidades o respuestas del servidor. La optimización se aplica a las representaciones nativas existentes; no selecciona otra pantalla ni omite la UI personalizada.

Los mapas viven en la sesión de resolución. No se añade caché global ni a ResourcePackStorage; se liberan con esa sesión. La profundidad deriva del árbol de controles ya limitado a 64 por el resolver. Las entradas siguen admitiendo claves/valores null y operaciones Map respaldadas por vistas.

## Pruebas

- 32 escenas reales de resolución son **idénticas byte por byte** entre IF48 y el experimento: 8 familias × 3 estados dinámicos de cantidad/durabilidad/bloqueos/hover/cursor, más herencia/modificaciones/ciclo/padre ausente personalizados × 2 estados.
- 10 grupos de contratos Map, incluidas 6.000 operaciones deterministas sobre 120 ramas, comprueban aislamiento, null, sobrescritura/borrado/clear, vistas, `Entry.setValue`, iteradores y compute/merge. Incluyen el caso de `controls` capturado antes de escribir `$__effective_visible`.
- La reversión canónica completa de las dos llamadas nuevas recupera la clase Session IF48 original. Todas las funciones de las cinco clases candidatas pasan BasicVerifier. Los cuerpos originales se ejecutan con `-Xverify:all` durante las pruebas de escenas.

## Rendimiento controlado

80 resoluciones por variante; se descartan 10 de calentamiento. JDK 25, `-Xverify:all`, JVMs separadas, sin JFR en las comparaciones pareadas. Asignación de memoria medida con ThreadMXBean, no estimada a partir del recolector:

| UI | Mediana IF48 | Mediana candidata | Bytes asignados IF48/candidata por resolución |
|---|---:|---:|---:|
| Cofre 27 | 34.084 ms | 20.350 ms | 74.207.280 / 30.173.792 |
| Cofre 54 | 45.998 ms | 30.402 ms | 103.925.040 / 41.654.528 |

La reducción observada es aproximadamente 34–40 % del tiempo del resolver y 59–60 % de la memoria asignada en este caso. **No es una medición de los FPS ni del equipo del usuario.** El programa procede de recursos incluidos; las presentaciones de slots son datos sintéticos explícitos. No se ejecutan NBT real de items, parseo/layout cliente posterior, fuentes/imágenes, GPU, Minecraft o Bedrock en estas mediciones.

Root aprobó esta candidata después de revisar su alcance y evidencia. `candidate-class-hashes.json` fija las cinco clases existentes en `experiment/classes`; el nombre del directorio describe su procedencia. Desde su aprobación no se reconstruyeron esas clases. Solo se quitó la palabra «Experimental» de un comentario de una línea del código fuente: no cambia su contenido ejecutable ni el número de líneas. `manifest.json` identifica un reemplazo y cuatro altas para el empaquetador.

La revisión independiente y el ensamblado final se gestionan por separado. Este agente no reconstruyó IF48 ni publicó un JAR.
