# Revisión de crafteos

Se comprobó la forma y cantidad de cada receta estática de dos fuentes con los métodos reales del core IF48. **Esto no certifica todas las recetas dentro del servidor ni sus resultados con NBT.**

| Fuente | Entradas catalogadas | Matching estático comprobado | Afirmaciones |
|---|---:|---:|---:|
| Muestras oficiales Bedrock 1.26.50.4 |1892|1655|38567|
| Referencia pública Bedrock 2193 |4768|4741|97852|

Las fuentes se solapan; no se suman como recetas únicas. La referencia pública no es la lista de la conexión del usuario. Las pruebas usan un predicado explícito de nombre/aux/tag con IDs simbólicos; no ejecutan resolución nativa, resultado/NBT, clics, ACK ni aceptación de servidor.

## Lo que sigue necesitando revisión específica

- Cohetes de vuelo 2/3: fallo reproducido; corrección de cohetes 1/2/3 con/sin estrellas en revisión. Crear una estrella nueva y añadir fade siguen siendo acciones distintas pendientes.
- Dinámicas MULTI: 14 UUID públicos, 13 semánticas identificadas y 1 desconocida. Clonar libros, reparar objetos, duplicar estandartes, mapas y vasijas no quedan corregidos por arreglar cohetes.
- USER_DATA: 1456 entradas pasan forma; eso no valida conservar contenido de shulkers, colores, texto o datos del resultado.
- Estaciones: 13 recetas públicas de herrería y 212 mezclas de pociones tienen datos/rutas identificados; el bulk no ejecuta sus transacciones. Tampoco certifica horno/cortapiedras/telar por probar un patrón.

## Cada receta, una por una

- [Catálogo oficial](CATALOGO-OFICIAL.md):1892 filas por familia, páginas de hasta 350 recetas; [CSV](CATALOGO-OFICIAL.csv) y [JSON](CATALOGO-OFICIAL.json).
- [Catálogo nativo público](CATALOGO-NATIVO-PUBLICO.md):4768 filas por familia; [CSV](CATALOGO-NATIVO-PUBLICO.csv) y [JSON](CATALOGO-NATIVO-PUBLICO.json).
- [Dinámicas y huecos](RECETAS-DINAMICAS.md), [mezclas de pociones](MEZCLAS-NATIVAS-PUBLICAS.json), [rutas y estaciones](RUTAS-Y-ESTACIONES.json).
- [Alcance, fuentes y reproducción](ALCANCE-Y-REPRODUCCION.md), [verificación](verification.json), [1307 nombres de ítem](native-item-mapping-ledger.json), [22 serializers Java](JAVA-SERIALIZERS.json), [tipos de receta Bedrock](BEDROCK-RECIPE-TYPES.json) y [descriptores](INGREDIENT-DESCRIPTORS.json).

Cada fila distingue la evidencia concreta de la prueba pendiente en servidor. No se modificó producción ni se publicaron archivos en esta subauditoría.
