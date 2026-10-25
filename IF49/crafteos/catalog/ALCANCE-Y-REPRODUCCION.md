# Revisión de todos los crafteos: catálogo y alcance real

Se revisó cada entrada de dos fuentes y se ejecutaron los métodos reales de matching del core IF48 sobre sus recetas estáticas. **Esto no certifica todos los crafteos dentro del servidor.** El caso comunicado de cohete de vuelo 2 es una receta dinámica: tener miles de patrones correctos no descarta ese fallo.

| Fuente | Entradas inventariadas | Recetas probadas por forma/cantidad | Afirmaciones del matcher real |
|---|---:|---:|---:|
| Muestras oficiales Bedrock 1.26.50.4 |1892|1655 (1064 shaped +591 shapeless)|38567|
| Referencia pública Bedrock 1.26.50, protocolo 2193 |4768|4741 (1217 shaped +2068 shapeless +1456 USER_DATA)|97852|

Las fuentes se solapan. No se suman como 6660 recetas únicas ni como una certificación de gameplay. El dump público no pertenece a la conexión del usuario: sus UUID/netIDs solo identifican la evidencia y no deben fijarse en producción.

## Una fila por entrada

- [Catálogo oficial:1892 filas](CATALOGO-OFICIAL.md), con [JSON](CATALOGO-OFICIAL.json) y [CSV](CATALOGO-OFICIAL.csv).
- [Catálogo nativo público:4768 filas](CATALOGO-NATIVO-PUBLICO.md), con [JSON](CATALOGO-NATIVO-PUBLICO.json) y [CSV](CATALOGO-NATIVO-PUBLICO.csv).
- [212 mezclas públicas de pociones/recipientes](MEZCLAS-NATIVAS-PUBLICAS.json), [rutas y estaciones](RUTAS-Y-ESTACIONES.json), [22 serializers Java](JAVA-SERIALIZERS.json), [1307 nombres de ítem](native-item-mapping-ledger.json).

Cada fila conserva tipo, estación, resultado de referencia, prueba concreta o ausencia de prueba, ruta y comprobación pendiente. Los resultados USER_DATA requieren conservar NBT; que coincidan los ingredientes no prueba contenido de shulker, colores, texto, encantamientos o datos de un cohete.

## Qué ejecuta el ensayo

`CraftingCatalogProbe.java` llama a `CraftingDataTracker.matchShaped`, `matchShapeless` y `ShapelessRecipe.expandIngredients` del JAR real IF48, sin reescribir sus algoritmos. Usa los modelos reales ShapedRecipe/ShapelessRecipe/ItemDescriptor/BedrockItem.

Por receta estática se comprueban formas 2×2/3×3 cuando caben, offsets, espejo habilitado en el modelo de prueba, orden inverso sin forma, ingrediente ausente, material ajeno, celda extra, stacks de 64, suma de unidades requeridas e inmutabilidad de los inputs. Un patrón mayor que 2×2 se rechaza en esa dimensión. Se conservan resultados individuales y cantidad de afirmaciones por fila.

La frontera del predicado de ingrediente es explícita: usa nombres/aux y miembros de tags del recurso real incluido, con IDs simbólicos. **No ejecuta ItemDescriptor.matchesItem conectado a un servidor ni su resolución de aliases.** Los nombres de fuentes JSON pueden ser históricos, no llevar namespace o codificar aux dentro del nombre; su ausencia literal en registros no prueba que la receta desaparezca.

Los 12 tags de ingredientes empleados en las formas públicas tienen miembros incluidos. Considerando también herrería/dinámicas hay 17 tags y 1307 nombres nativos de ítem: todos tienen declaración runtime/mapping y destinos existentes en el registro Java incluido. Este último control solo verifica datos, no sus usos ni NBT.

Cincuenta patrones oficiales de escalera tienen filas de distinta longitud; se rellenan espacios a la derecha para formar la matriz del fixture. No se cambia la fuente ni se presenta como fallo del addon. Dos recetas oficiales, sopa de remolacha y lingote de netherita, también coinciden con cantidades comprimidas en dos celdas del matcher 2×2. Se registra la observación; no se atribuye a Bedrock una aceptación/rechazo que no se midió.

## Familias que el bulk de cuadrícula no certifica

La muestra oficial contiene 152 recetas de horno, 72 de pociones y 13 de herrería fuera del matcher de cuadrícula. La referencia pública contiene 14 MULTI y 13 de herrería fuera del bulk, además de 210 mezclas de pociones y 2 cambios de recipiente separados. No figuran como pruebas de forma aprobadas.

De 14 UUID MULTI públicos se identificaron 13 por fuentes fijadas de PMMP/PowerNukkitX: reparación, fuegos artificiales, clonado de libros, operaciones de mapas, patrones/duplicado de estandartes y vasija decorada. `00000000-0000-0000-0000-0000000000c8` queda desconocido: no se inventa que sea decoración de escudo. FIREWORKS usa un UUID común; no se deducen tres UUID separados de los nombres Java de cohete/estrella/desvanecimiento.

El core genérico guarda MultiRecipe como UUID sin patrón/resultado y su matcher normal solo trata SHAPED/SHAPELESS/USER_DATA_SHAPELESS. La familia necesita lógica específica que preserve datos y use el networkId realmente anunciado. La auditoría de fuegos artificiales es la que reproduce vuelo 2/3 sin preview y pérdida de NBT de estrella al usar solo resultado estático. Este catálogo no presenta esa corrección como entregada mientras su revisión siga en curso.

El registro Java incluido enumera 22 serializers, incluidos crafting_dye/imbue/transmute y especiales. Su presencia **no prueba** que exista conversión Bedrock ni que esas recetas se ejecuten. Lo mismo se aplica a las constantes UNKNOWN de RecipeType y a las familias de química: se documenta su lector, sin fingir que las fuentes vanilla aquí contienen todas las recetas Education.

## Fuentes fijadas

- [Mojang/bedrock-samples](https://github.com/Mojang/bedrock-samples/tree/46ba6ea985fb5a92d79a9419198f10dda14c199d/behavior_pack/recipes), commit 46ba6ea985fb5a92d79a9419198f10dda14c199d, versión 1.26.50.4. 1892 archivos originales con SHA256 por entrada.
- [Kaooot/bedrock-network-data](https://raw.githubusercontent.com/Kaooot/bedrock-network-data/3e50b56363888330800588a0bd6798c508416659/release/1.26.50/recipes.json), commit 3e50b56363888330800588a0bd6798c508416659. SHA256 `8b25b59c2ecff6942f81df48be9fe1e69ca85083170036844f0e45816bff8514`. El tipo numérico de ese JSON es del formato del autor: 0 shaped, 1 shapeless, 2 multi, 3 userdata, 6 transform, 7 trim. No se confunde con los valores de RecipeType del paquete de red.
- Semánticas MULTI: [MULTI-SEMANTICS.json](MULTI-SEMANTICS.json), con URL y commit de las constantes identificadas.
- Base IF48 y clases exactas fijadas en `verification.json`; no se modificó producción ni se publicaron archivos durante esta auditoría.

## Reproducción

El classpath guardado coloca core-if48.jar antes de los JAR históricos. Se usa JDK 25.0.5.0.2 con `-Xverify:all`. Los IDs/UUID del modelo de prueba son fixtures y nunca se empaquetan como constantes runtime.

```sh
javac -cp "$(cat classpath.txt)" -d test-classes CraftingCatalogProbe.java
java -Xverify:all -cp "test-classes:$(cat classpath.txt)" net.raphimc.viabedrock.protocol.storage.CraftingCatalogProbe official-catalog-input.json matcher-results.json
java -Xverify:all -cp "test-classes:$(cat classpath.txt)" net.raphimc.viabedrock.protocol.storage.CraftingCatalogProbe native-reference-input.json native-matcher-results.json
python build_coverage_ledger.py
```

No distribuir test-classes como clases cargables del mod. Fuentes de prueba y resultados son evidencia separada. El cierre/clic rápido, autocolocación, slots compartidos, recipientes devueltos y ACK/rechazo se auditan en las rutas de inventario; no quedan certificados por esta prueba de formas.

Los paquetes JSON originales y los fixtures que los contienen no se incluyen en la entrega: [SOURCE-MANIFEST.json](SOURCE-MANIFEST.json) fija URLs y hashes para recuperarlos. `prepare_catalog.py` y `prepare_native_reference.py` generan los inputs locales del ensayo a partir de esas fuentes; `CraftingCatalogProbe.java` produce los resultados derivados publicados.
