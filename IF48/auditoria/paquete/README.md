# Auditoría independiente del JAR final IF48

**PASS**. Se revisó el artefacto existente, sin ejecutar/importar su constructor, reconstruir el mod, publicar ni repetir pruebas históricas.

- JAR: `ViaFabricPlus-Bedrock-IF48.jar`.
- Tamaño: **215635312 bytes**.
- SHA256: **`cce72ee547155865c5196b0262deec9766bb9267741a755eaa72bbad74ae758a`**.
- Base IF47 fijada: `8045b1222551c574a389bc8ec6c63f84c12bba3aa7c7d616480fe1762fdac15a`.

`audit_if48.py` es una comprobación independiente basada únicamente en ZIP/CRC/SHA256 y los ficheros allowlist; no reutiliza el código del constructor.

## Alcance exacto

El archivo exterior contiene1222 entradas:1218 permanecen byte por byte,3 cambian (metadatos Fabric, core anidado y política de arco) y se añade únicamente `META-INF/stackanvil-if48-changes.zip`. Los metadatos Fabric solo cambian nombre y versión a `1.1.1-StackAnvil-InteractionFix48.1`.

El core contiene1531 entradas:1527 permanecen byte por byte; únicamente cambian `ConnectedBlockShapes` y `ClientPlayerPackets`, y se añaden `ChorusPlantConnections` y `EndCreditsBridge`. Las cinco clases runtime coinciden exactamente con las candidatas autorizadas por SHA256. Las cuatro clases del observador XP conservan sus hashes IF47 exactos; no aparece una corrección nueva de XP. Los hashes protegidos de las correcciones anteriores se contrastaron contra ambos archivos, además de la comparación completa de todas las entradas.

CRC y unicidad de nombres pasan en ambos archivos exteriores, ambos cores y el ZIP nuevo de fuentes. No se añaden fixtures/clases de prueba cargables ni archivos de Minecraft de referencia.

## Fuentes y documentación

El ZIP nuevo contiene exactamente92 archivos:88 seleccionados por hash y4 archivos automáticos de construcción. Cada hash incrustado coincide con el manifiesto congelado y con su archivo local. Se excluyen mundos, medios, logs privados, archivos binarios de pruebas, repositorios y fuentes completas de referencias/terceros.

El informe Markdown de246 filas conserva SHA256 `c10b51fbf050ab804954df3472d422b5cad8ea644bbab41928ff2e3da75b6f12`; JSON/CSV tienen las mismas246 IDs y el mismo orden. Su declaración de que no existe certificación global del juego permanece explícita.

Se leyó el README de entrega incrustado, SHA256 `633541c47f617110a08b541dabc1fce370dbabffaf619b0d666e7d5b74eaa735`. Describe tres correcciones concretas y60 grupos dirigidos con sus límites; no afirma que el dragón completo esté certificado. Explica que XP y mapas continúan pendientes, conserva el prefijo `[IF47-XP]`, exige un único addon IF y conserva ViaFabricPlus base. No afirma ejecución de gráficos, Mixin o servidor Bedrock.

Esta revisión final confirma integridad, alcance y coherencia documental del artefacto. No añade una prueba funcional en Minecraft ni certifica combate, teletransportes, inventarios reales o aceptación del servidor.
