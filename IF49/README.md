# IF49 — cofres y crafteo de cohetes

**[Descargar IF49: mod completo](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF49/ViaFabricPlus-Bedrock-IF49.jar)**

**[Revisión completa del catálogo de crafteos](crafteos/catalog/README.md)**

## Cambios

- **Cofres:** evita preparar dos veces la misma interfaz al abrirla y reduce las copias temporales de datos que esa preparación necesitaba. Si cambia el contenido, el tamaño, la fuente, el título o el estado de la pantalla, se conserva la reconstrucción normal. Los clics y las transacciones del inventario mantienen sus rutas anteriores.
- **Cohetes:** añade la ruta especial de Bedrock que faltaba para papel con uno, dos o tres espacios de pólvora. Produce tres cohetes por operación, con duración 1, 2 o 3. También conserva las explosiones de las estrellas que se añadan al cohete. La receta de la captura, con papel y dos espacios de pólvora, queda cubierta.
- La fabricación usa el identificador de receta anunciado por el servidor. Conserva las confirmaciones, los rechazos y la continuación de fabricación con Shift de las versiones anteriores.

Es el JAR completo, sin actualizador. Conserva IF48 y las correcciones anteriores, además del diagnóstico de XP de IF47.

## Instalación

1. Cierra Minecraft por completo.
2. Sustituye el addon **ViaFabricPlus Bedrock IF** anterior de `mods` por este JAR. Deja un solo addon Bedrock IF y conserva ViaFabricPlus base.
3. Abre Minecraft y confirma **ViaFabricPlus Bedrock - IF49**, versión `1.1.1-StackAnvil-InteractionFix49.1`.
4. Prueba las aperturas de cofres y la receta de la captura. Una pila de papel y dos pilas de pólvora en tres espacios deben mostrar cohetes de duración 2; cada operación consume una unidad de cada espacio y produce tres cohetes.

## Qué se comprobó

Las pruebas de cofres comparan las clases reales de resolución de interfaz y el control de la preparación inicial. En la prueba aislada, el resolver asigna aproximadamente un 60 % menos de memoria temporal y tarda un 34–40 % menos. Además se evita repetirlo en la primera apertura cuando la preparación sigue vigente. **Estos números no son FPS ni mediciones del equipo del usuario.**

Las pruebas de cohetes ejercen la búsqueda de receta, la vista previa, clic normal, Shift, cantidades, NBT y las peticiones/respuestas de inventario. La comparación reproduce el resultado vacío anterior para duración 2 y 3.

La revisión de crafteos incluye todas las entradas de dos fuentes fijadas: 1.892 archivos oficiales y 4.768 entradas de un catálogo público del protocolo Bedrock 2193. Las fuentes se solapan; no se suman como recetas únicas. Se probaron las formas de 1.655 recetas oficiales y 4.741 entradas estáticas del catálogo público. También se documentan pociones, herrería, tipos especiales y sus límites, con una fila por entrada y páginas separadas por familia.

**No se probó cada receta dentro de un servidor real.** La coincidencia de ingredientes no certifica por sí sola el resultado con NBT, las transacciones ni la aceptación del servidor. Los informes separan esas comprobaciones.

## Pendientes

El cambio de cohetes no implementa todas las recetas especiales de Bedrock. Crear estrellas, añadir colores de desvanecimiento y otras familias dinámicas tienen límites o rutas pendientes, descritos en el catálogo. No se declara corregido todo el crafteo.

La XP lenta, los mapas y los problemas pendientes del dragón señalados en IF48 continúan pendientes. IF49 conserva el registro limitado de XP con el prefijo `[IF47-XP]`; para esa investigación sigue siendo útil `logs/latest.log` después de reproducir el problema enseguida al entrar al servidor.

No se ejecutaron Minecraft gráfico, el motor Mixin ni un servidor Bedrock en este entorno. Falta confirmar en la partida del usuario el resultado de la corrección de los tirones y del crafteo.
