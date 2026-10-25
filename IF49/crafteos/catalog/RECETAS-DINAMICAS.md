# Recetas dinámicas: cobertura y huecos

No se deduce soporte por encontrar un serializer o UUID. Los netIDs se anuncian por conexión; los de la referencia pública no son constantes de producción.

| UUID | Semántica de fuente fijada | Ruta y alcance actual | Comprobación pendiente |
|---|---|---|---|
| `442d85ed-8272-4543-a6f1-418f90ded05d` | TYPE_MAP_CLONING_CARTOGRAPHY | CartographyRecipes.select contiene UUID dedicado; no certificado por el matcher general. | Operación real de mapa, consumo, nombre y NBT. |
| `8b36268c-1829-483c-a0f1-993b7156a8f2` | TYPE_MAP_EXTENDING_CARTOGRAPHY | CartographyRecipes.select contiene UUID dedicado; no certificado por el matcher general. | Operación real de mapa, consumo, nombre y NBT. |
| `602234e4-cac1-4353-8bb7-b1ebff70024b` | TYPE_MAP_LOCKING_CARTOGRAPHY | CartographyRecipes.select contiene UUID dedicado; no certificado por el matcher general. | Operación real de mapa, consumo, nombre y NBT. |
| `98c84b38-1085-46bd-b1ce-dd38c159e6cc` | TYPE_MAP_UPGRADING_CARTOGRAPHY | CartographyRecipes.select contiene UUID dedicado; no certificado por el matcher general. | Operación real de mapa, consumo, nombre y NBT. |
| `d81aaeaf-e172-4440-9225-868df030d27b` | TYPE_BANNER_ADD_PATTERN | LoomContainer/LoomPatterns tienen ruta propia de telar; no asumir mismo soporte en mesa por UUID. | Conservar patrones/colores al fabricar y confirmar resultado servidor. |
| `b5c5d105-75a2-4076-af2b-923ea2bf4bf0` | TYPE_BANNER_DUPLICATE | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `00000000-0000-0000-0000-000000000002` | TYPE_FIREWORKS | Fallo de cohetes Flight 2/3 reproducido. Corrección en revisión para cohetes Flight 1/2/3 con/sin estrellas; creación de estrellas y fade siguen fuera de esa corrección. | Validar helper, acciones y aceptación nativa; probar estrella nueva, forma/color/efectos y fade por separado. |
| `d1ca6b84-338e-4f2f-9c6b-76cc8b4bd98d` | TYPE_BOOK_CLONING | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `85939755-ba10-4d9d-a4cc-efb7a8e943c4` | TYPE_MAP_CLONING | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `685a742a-c42e-4a4e-88ea-5eb83fc98e5b` | TYPE_DECORATED_POT_RECIPE | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `d392b075-4ba1-40ae-8789-af868d56f6ce` | TYPE_MAP_EXTENDING | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `00000000-0000-0000-0000-000000000001` | TYPE_REPAIR_ITEM | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |
| `00000000-0000-0000-0000-0000000000c8` | UNKNOWN_DO_NOT_INFER | Desconocida. No atribuirla a escudo ni otra familia sin evidencia. | Identificar contrato nativo antes de generar solicitudes o resultados. |
| `aecd2294-4b94-434b-8667-4499bb2c9327` | TYPE_MAP_UPGRADING | Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes. | Implementación específica, resultado/NBT/remainders y transacción aceptada. |

Los 14 UUID proceden del dump público 2193; 13 semánticas se documentan en constantes PMMP/PowerNukkitX fijadas por commit. No son una captura de la sesión del usuario.
