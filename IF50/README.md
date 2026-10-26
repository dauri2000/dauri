# IF50 — inventario, crafteos y compatibilidad de objetos

**Mod completo listo para instalar, sin actualizador.** Conserva IF49.1 y añade las correcciones reunidas durante esta revisión.

> Esta entrega responde al nuevo plazo que pediste. La revisión completa de todos los crafteos, el End y el stronghold sigue abierta. Las pruebas descritas abajo tienen un alcance concreto.

**[Descargar IF50: mod completo](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF50/ViaFabricPlus-Bedrock-IF50.jar)**

Versión: `1.1.1-StackAnvil-InteractionFix50.1` · Tamaño:216.155.620 bytes (206,14MiB).

SHA256: `7457e2f4e3383684917e9f3add41c85387fcba351b1561eb9dced60c328ff309`.

## Cambios comprobados dentro del servidor

- **Al cerrar la mesa**, los ingredientes y sobrantes vuelven al inventario. Se comprobó con un tablón y con los tres cubos vacíos del pastel, sin recuperarlos manualmente.
- **Objetos sin NBT:** conserva sus restricciones de colocación. El tablón restringido mantiene sus siete restricciones y queda separado de la pila normal al moverlo, cerrar y reabrir la mesa.
- **Libros firmados:** evita convertir el título que devuelve Bedrock en un nombre artificial del objeto durante los movimientos del inventario. El libro conserva su NBT original.
- **Recetas con sobrantes:** incluye la corrección de las solicitudes con dos resultados y su continuación con Mayús. Hay muestras aceptadas de azúcar desde miel y bloque de miel, con devolución de botellas; no certifica todas las recetas o variantes.

## Otras correcciones incluidas

El paquete reúne las correcciones revisadas de asociación de partes del dragón; estados de entidades; animación y selección del modelo del arco; traducción de datos de objetos, pociones, libros encantados y contenido de cajas shulker; máximos de durabilidad; datos de bloques y estaciones; familias de recetas especiales y orden de entrada de cartografía. La caché de recursos convertidos cambia de revisión para reconstruir automáticamente los packs antiguos que corresponda.

Estas rutas pasan pruebas controladas con las clases y el cliente exactos. Esto no convierte cada una en una mecánica comprobada en la partida del usuario.

## Instalación

1. Cierra Minecraft por completo.
2. En la carpeta `mods` de la instancia Prism, sustituye el addon **ViaFabricPlus Bedrock IF** anterior por **ViaFabricPlus-Bedrock-IF50.jar**. Deja un solo addon Bedrock IF y conserva **ViaFabricPlus base**.
3. Abre Minecraft y comprueba en Mods que aparece **ViaFabricPlus Bedrock - IF50** y la versión indicada junto a la descarga.

Se instala en el cliente Java/Fabric. No se coloca en la carpeta de plugins del servidor Bedrock Dedicated Server.

## Qué se comprobó

El laboratorio usa Minecraft Java26.3/Fabric con Java25 y Bedrock Dedicated Server oficial1.26.52.3. La integración de51 clases pasa las10 suites de regresión completas; las51 procedencias y hashes cargados también se contrastaron. El número51 describe la integración interna, no otra versión pública del mod.

El catálogo contiene4.768 entradas. Hay muestras reales de cinco familias: cohetes de duración2, botón de madera, azúcar desde miel, bloque de miel y pastel. **No se ha fabricado cada receta dentro de BDS.**

La devolución de la mesa y los movimientos de objetos y libros tienen confirmaciones y contenidos completos nativos registrados. Una revisión independiente contrastó31 buffers originales con dos lectores y reserialización idéntica.

En el End se observaron las ocho partes Java del dragón asociadas al mismo actor Bedrock. Un golpe normal al cuerpo produjo vida nativa200→198,75. La prueba usó modo creativo y llegada por teletransporte administrativo: no acredita combate completo en supervivencia ni entrada por el portal. La primera ventana del ataque está completa; las pérdidas posteriores de captura limitan el resto de esa sesión.

La flecha fue visible con arco normal y encantado en las pruebas gráficas reales. La prueba normal también consumió munición; la captura del encantado no acredita su consumo.

## Pendientes

- Completar todas las recetas y variantes, incluidas las familias dinámicas de libros/estandartes, reparación manual, estrellas, mapas y transacciones de las estaciones.
- Completar fases del dragón, cristales, resurrección, aliento, recompensas, portales, gateways, ciudades, vuelo y todas las interacciones del End; además del recorrido y las mecánicas del stronghold.
- Reproducir y resolver el retraso de XP de hasta20 segundos. El control observado recogió6 XP en unos3,15 segundos y no reproduce ese fallo.
- Contrastar el arco con los ZIP exactos de **Bedrock Flavor13.0.0** y **GUI Overhaul3.1** que aparecen en el log del usuario. Su contenido aún no se ha revisado; no se afirma que causen el fallo.
- Verificar de forma completa los casos reportados de colocación/orientación, agua dentro de bloques y ghast feliz. Esta entrega no los declara todos resueltos.

No se procesó el mundo Vega. El JAR distribuido no debe contener observadores, controles o diagnósticos privados del laboratorio.
