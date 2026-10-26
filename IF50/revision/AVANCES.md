# Revisión en curso — 26 de octubre de 2026

**IF50 todavía no tiene una entrega ni una descarga.** La revisión de todos los crafteos, el End y el stronghold continúa. Los candidatos y las pruebas están guardados; ninguna comprobación pendiente se considera aprobada por tener un mapping o una vista previa.

Se conserva intacto el [mod anterior IF49](../../IF49/README.md), versión `1.1.1-StackAnvil-InteractionFix49.1`, SHA256 `93ad74e03ef84237bd4ea376d5a2cb8837d730308e0f5305562f5652af63df5a`. No se ha generado otro JAR ni un actualizador.

**Crafteos comprobados en el servidor real.** El laboratorio utiliza Minecraft Java26.3/Fabric y Bedrock Dedicated Server oficial1.26.52.3. Hay muestras de cinco familias: cohetes de duración2, botón de madera, azúcar desde miel, bloque de miel y pastel. Cuatro familias tienen22 solicitudes aceptadas con bytes y confirmaciones del servidor registrados. El pastel tiene una comprobación distinta: persistieron un pastel y tres cubos al reconectar, con consultas independientes de cantidades al servidor. No se capturó su confirmación original ni se le asigna una solicitud supuesta. Estos resultados no equivalen a fabricar todas las recetas ni todas sus variantes.

La revisión del catálogo nativo abarca4.768 entradas. Las pruebas controladas recorren ingredientes, resultados, datos de objetos, clic normal, Mayús y reversión de rechazos. Se distinguen del juego real; el [catálogo publicado anteriormente](../../IF49/crafteos/catalog/README.md) también conserva sus límites históricos.

**Fallos concretos encontrados.** El pastel produce sus tres cubos, pero al cerrar la mesa los ingredientes y sobrantes pueden quedarse en la cuadrícula nativa en lugar de volver a la bolsa. Se reprodujo con cubos y con un tablón, tanto cerrando normalmente como con Escape. Reabrir permite recuperarlos. Se identificó una ruta de devolución omitida; su corrección sigue en preparación y validación, sin incorporarse al candidato actual.

También falta la vista previa de reparación para lanza de cobre más lingote de cobre, aunque la definición nativa declara ese material. La corrección requiere contrastar el resultado y redondeo aceptados por BDS. Los libros firmados y los estandartes decorados ya se obtuvieron mediante las interfaces reales de libro y telar y conservaron sus datos al reconectar; esto prepara las pruebas de clonado y duplicación, que siguen pendientes.

**End y stronghold.** Se ampliaron las fuentes al contenido archivado dentro del servidor:597 archivos y8.811 entradas indexadas. El plan conserva246 filas originales, añade17 desgloses y495 subcasos de estructuras, botín, eventos de mobs, bloques transportables por Enderman y variantes de objetos. Es una lista de comprobaciones, no495 mecánicas jugadas y aprobadas. Siguen pendientes combate y fases del dragón, resurrección, portales, viajes, recompensas, interacciones y recorridos del stronghold. La flecha del arco aparece en una escena gráfica controlada con el candidato; falta completar su comprobación nativa.

**Experiencia.** El código exacto confirma que Java simula física y atracción de los orbes en cliente. Todavía no demuestra que eso cause el retraso descrito. Se preparó observación correlacionada de posiciones y XP; no se concede experiencia ni se ocultan orbes artificialmente.

Las capturas, paquetes y pruebas detalladas permanecen en el espacio de trabajo privado. No se publican cuentas, logs, binarios del juego ni mundos. No se procesa el mundo Vega. Antes de una descarga se concretará cualquier dato del usuario que realmente falte; la entrega futura será el mod completo con enlace HTTPS verificado y guardado.
