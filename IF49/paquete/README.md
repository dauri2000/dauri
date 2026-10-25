# IF49: revisión independiente del archivo final

**PASS.** Se inspeccionó el JAR final inmutable, sin importar el constructor, reconstruir producción ni repetir pruebas funcionales.

- Archivo: `ViaFabricPlus-Bedrock-IF49.jar`, **216017725 bytes**.
- SHA256: `93ad74e03ef84237bd4ea376d5a2cb8837d730308e0f5305562f5652af63df5a`.
- Base IF48: `cce72ee547155865c5196b0262deec9766bb9267741a755eaa72bbad74ae758a`.
- CRC válidos y ausencia de entradas duplicadas en ambos JAR principales, ambos core y el nuevo ZIP de fuentes.
- Cambios exactos: una pantalla cliente, Session, cuatro clases de snapshot y matcher/helper de cohetes: **ocho entradas runtime**. Todas coinciden con sus hashes congelados y archivos locales. Las demás entradas anteriores conservan contenido idéntico.
- Metadata: únicamente nombre y versión pasan a IF49/`InteractionFix49.1`.
- **37 entradas protegidas** permanecen intactas; se comprobaron también las cinco correcciones IF48 y los cuatro observadores XP de IF47.
- ZIP de fuentes: **137 archivos**, 131 de la lista explícita y seis automáticos; nombres y hashes coinciden exactamente con la lista permitida y archivos locales.
- No se añadieron medios, logs privados, mundos, JFR, paquetes originales de referencias, clases cargables de prueba ni clases Minecraft.
- Las **29 páginas** del catálogo contienen todas las filas: 1892 oficiales y 4768 de referencia pública, hasta 350 por página. Las seis tablas/resultados derivados mayores de 1 MiB omitidos del ZIP conservan nombre, tamaño y SHA256 completos en el manifiesto incorporado; su publicación separada no forma parte de esta comprobación.

La guía distingue las tres implementaciones: reutilizar preparación inicial de interfaz, reducir copias del resolver y fabricar cohetes de duración 1/2/3 con estrellas existentes opcionales. No presenta los tiempos aislados como FPS del usuario ni certifica todas las recetas. Mantiene pendientes creación de estrellas, fade y otras familias MULTI, XP, mapas y dragón. El diagnóstico XP conserva `[IF47-XP]`.

`audit_if49.py` lee y compara bytes, índices y documentos. `verification.json` guarda resultados, hashes por entrada y el detalle de los seis archivos derivados omitidos. No ejecuta Minecraft, Mixin ni BDS y no valida una partida o una descarga pública.

```sh
python audit_if49.py
```
