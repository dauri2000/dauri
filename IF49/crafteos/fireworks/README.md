# IF49: cohetes de fuegos artificiales y revisión de recetas MULTI

## Problema demostrado

La captura del usuario muestra papel y dos casillas con pólvora, con pilas de32. Esa combinación debe producir tres cohetes de duración2, consumiendo un papel y una pólvora de cada casilla por fabricación.

En el core IF48 real, `CraftingDataReader` conserva recetas MULTI enviadas por el servidor, pero `CraftingDataTracker.getRecipeMatch` solo procesa SHAPED/SHAPELESS/USER_DATA_SHAPELESS y exige una etiqueta de estación que MULTI no tiene. La preview queda vacía para combinaciones dinámicas. Con una estrella personalizada, una receta estática de ejemplo puede producir una preview con la explosión predeterminada en vez de conservar la estrella real.

`FireworkIF48BaselineProbe` reproduce ambos problemas en IF48 utilizando IDs de objetos del runtime_item_states entregado y las recetas públicas del **mismo protocolo2193**. El dump no pertenece a la sesión del usuario: sirve como entrada de protocolo reproducible. Los 13 checks de baseline incluyen las reproducciones esperadas del fallo y no se contabilizan como correcciones.

## Corrección candidata y límites

Solo se reemplaza `CraftingDataTracker.class` y se añade `FireworkMultiRecipes.class`. El cambio de tracker es un adaptador al retornar de getRecipeMatch. El verificador elimina exactamente sus siete instrucciones y recupera la clase IF48 canónica completa; todos los otros métodos, campos y atributos se conservan.

El adaptador se activa exclusivamente si el servidor anunció MULTI con UUID **00000000-0000-0000-0000-000000000002**, identificado como TYPE_FIREWORKS en una referencia PMMP fijada por commit. Usa el **network ID actual anunciado por el servidor**, nunca el número de ejemplo de un dump ni un identificador inventado. Si se retira la familia de recetas, se retira la preview dinámica; un anuncio nuevo cambia inmediatamente el identificador utilizado.

Para un papel, una a tres casillas de pólvora y cero o más estrellas que quepan en la cuadrícula:

- Calcula tres cohetes, Flight1/2/3 según casillas de pólvora, con una unidad consumida por casilla y fabricación.
- Copia profundamente cada `FireworksItem` real a `Fireworks.Explosions`, en el orden de la cuadrícula. Conserva color, fade, forma, trail, flicker y campos nativos adicionales. No pasa por los colores RGB Java ni arrastra customColor de la raíz de la estrella.
- Devuelve un ShapelessRecipe **efímero**, con el UUID y network ID anunciados, para utilizar las rutas existentes de preview, clic, petición, respuesta, rollback y continuación IF39. No registra una receta nueva en la lista ni en el libro.
- Conserva una receta explícita del servidor que produzca un objeto distinto de cohete. También conserva recetas personalizadas de cohete fuera de la cardinalidad vanilla, por ejemplo dos papeles. No transforma una receta explícita que produzca una estrella en una receta de cohete.
- Rechaza una estrella de la combinación dinámica si no tiene el formato nativo validado: FireworksItem compuesto, colores/fade ByteArray válidos, tipo Byte0..4 y flags Byte booleanos. Colores0..15; arrays limitados a256 entradas para acotar el adaptador. No se afirma compatibilidad con todas las extensiones NBT de servidores personalizados.

**Crear estrellas complejas y añadir colores de desvanecimiento no se implementa dinámicamente en esta corrección.** Las referencias identifican la familia común, pero no verifican todas sus cardinalidades, auxiliary data y customColor nativos. Las recetas estáticas existentes de estrella permanecen disponibles. Tampoco se implementan indiscriminadamente las otras trece MULTI: `reference/multi-uuid-semantics.json` distingue13 UUID conocidos y uno desconocido; conocer un nombre no prueba que su algoritmo esté soportado.

## Verificación de la candidata final

Hashes definitivos:

```text
CraftingDataTracker.class b3dee566f527d0465a853f877a368b0151a6cf3fb72d00e3f6dc722d5054d153
FireworkMultiRecipes.class 27f6da94517f6f5a2467a60be89c10926716648186b98d607e60a0b69b9a2126
```

**59 checks de comportamiento/política:**

- 27 combinaciones de preview: todas las cantidades de estrellas que caben con Flight1/2/3 en 3×3 y 2×2, con pilas de32 y posiciones dispersas. Comprueban resultado, network ID cambiado respecto al ejemplo, consumo por casilla y copia exacta del NBT de entrada.
- 8 ciclos de clic normal/Shift, aceptación/rechazo, en ambas cuadrículas. Ejecutan los handlers existentes, comparan la petición con un modelo de respuesta suministrado y comprueban los IDs actuales por slot, NBT, predicción y rollback.
- 2 continuaciones con un único Shift:21+11 fabricaciones serializadas producen96 cohetes. No se cambió el código IF39.
- 22 límites de política: ausencia/cambio de anuncio, netId0, otra estación, recetas explícitas personalizadas de diamante/cohete/estrella, pólvora excesiva y doce variantes de estrella malformada.

Dos verificaciones adicionales de alcance: reversión canónica completa y ASM BasicVerifier en todos los métodos de las dos clases candidatas.

La revisión independiente ejecutó **15 grupos adicionales** sobre esos mismos hashes, sin reconstruir las candidatas:12 grupos de handlers, formato binario y límites, y3 de aislamiento bytecode. Dentro de esos grupos codificó realmente9 peticiones nativas y comprobó IDs, consumos por slot, resultado NBT y referencias CreatedOutput. Las nueve codificaciones no se vuelven a sumar como nueve pruebas extra. También comprobó que un ACK aislado y solo tres de las cuatro actualizaciones completas no reanudan el bulk. Evidencia: `independent/verification.json`.

**Sincronización NBT preservada:** las rutas existentes solicitan una actualización completa para objetos fabricados que tienen NBT. Las pruebas de aceptación suministran las notificaciones autoritativas de inventario/HUD/armadura/mano secundaria antes de permitir la continuación. No se afirma que un ACK aislado basta ni se eliminó esa espera.

Estas pruebas usan clases de producción reales del core IF48 más las dos candidatas. Las fronteras son conexión/transporte, representación Java de objetos y respuestas autoritativas modeladas. El ItemRewriter de la fixture usa los IDs nativos reales para matching; su salida Java gráfica está vacía deliberadamente y no se usa para decidir el resultado nativo. **No se ejecutó Minecraft gráfico ni BDS, ni se comprobó aceptación en el servidor del usuario.** La codificación binaria independiente tampoco ejecuta el decodificador ni la validación de un servidor nativo.

## Fuentes y reproducción

Fuentes con URL/commit/SHA256: `reference/download-manifest.json`. El dump público es de Kaooot1.26.50, protocolo2193, con4768 recetas,14 MULTI y41 recetas explícitas relacionadas con fuegos artificiales; solo Flight1 aparece de forma estática. No se incorporan mundos, vídeos o logs privados.

Archivos:

- `baseline-classes.json`, `candidate-class-hashes.json`, `verification.json` y `manifest.json`: versiones y alcance.
- `baseline-output.txt`: 13 checks del IF48 sin corregir.
- `candidate-output.txt`: 37 checks de preview/handlers/continuación.
- `policy-output.txt`: 22 límites de compatibilidad y validación.
- `scope-output.txt`: dos pruebas de alcance bytecode.
- `src/.../FireworkMultiRecipes.java` y `PatchFireworkMultiRecipes.java`: implementación.
- `Firework*Probe.java`, `VerifyFireworkPatchScope.java`: pruebas.

El classpath se guarda en `classpath.txt`. El harness de transporte reutilizado es `RapidCraftOutputIF38Regression.java`, con SHA en manifest; no sustituye clases del juego. Las candidatas deben ir antes del core IF48 en el classpath.

```sh
javac -cp "test-classes:candidate:$(cat classpath.txt)" -d test-classes FireworkCandidateProbe.java FireworkPolicyProbe.java
java -Xverify:all -cp "test-classes:candidate:$(cat classpath.txt)" FireworkCandidateProbe reference/kaooot-1.26.50-recipes.json
java -Xverify:all -cp "test-classes:candidate:$(cat classpath.txt)" FireworkPolicyProbe reference/kaooot-1.26.50-recipes.json
```

No se construyó ni publicó un JAR completo desde esta subauditoría; la integración y la entrega final corresponden al agente principal.
