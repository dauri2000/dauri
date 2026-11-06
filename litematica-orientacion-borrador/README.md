# Borrador de orientación y Easy Place

Fuente de trabajo para Minecraft Java 26.3 y BDS 1.26.60.6. **No contiene un mod instalable ni acredita que todos los bloques funcionen dentro del juego.** El TEST1 de materiales publicado antes no incluye estos cambios.

## Cambios

- Integra la ruta con `easyPlacePostRewrite` activado; conserva la elección del usuario y la ruta Java.
- Convierte los clics relativos una sola vez, busca soportes mediante rayos reales y comprueba el resultado de colocación, orientación, ejes, mitades y agua.
- Conserva la orientación de cada acción y serializa las colocaciones. Espera un envío normal de fin de tick posterior a la preparación antes de usar el bloque; un callback END por sí solo ya no basta.
- Compara el tipo de cofre y la sujeción del cartel colgante. Permite la primera mitad sencilla de un cofre doble cuando su pareja esquemática es válida.
- Coloca primero la maceta vacía y permite insertar la planta en otra activación, con una acción independiente y sus comprobaciones de contexto e inventario.

## Verificación

Las 35 fuentes principales compilan sin errores ni avisos con el cliente suministrado, Fabric Loader 0.19.5 y su MixinExtras 0.5.5. Se probó transformación real con Sponge/Knot en un lanzador de pruebas declarado; eso no equivale a arrancar el juego completo.

| Prueba dirigida | Resultado y alcance |
| --- | --- |
| Losas, clics y contexto de orientación | 57 + 26 + 79 casos; métodos reales con los mocks indicados en cada recibo |
| Entrada del jugador | 256 máscaras y 256 recorridos del codec; conserva los otros seis bits |
| Cofres y carteles | 48 comprobaciones nuevas; los cuatro pares incorrectos del test anterior ya se rechazan |
| Macetas | 39 plantas y 2 controles; método de uso real con mundo/jugador simulados explícitamente |
| Sincronización | El traductor real reproduce la mirada antigua; el FIFO corregido espera el siguiente envío normal |

El inventario de 1.286 tipos registra rutas y pendientes; no representa 1.286 colocaciones aprobadas. El detalle técnico está en `NOTAS-TECNICAS.md` y el estado del borrador en `estado.json`.

## Pendiente

Faltan el arranque del conjunto completo y las comprobaciones autoritativas en BDS. También siguen pendientes fases de agua, pastel con vela, calderos llenos, transiciones de plantas, cantidades e interacciones posteriores, sustituciones LOOSEN y uso manual inmediatamente posterior. Ninguna descarga de orientación se publica mientras esas comprobaciones requeridas sigan abiertas.

Se conservan la licencia MIT de EasyPlaceFix y los originales. Los binarios de Minecraft, mundos, grabaciones, registros del usuario y pruebas privadas quedan fuera de este directorio. La compilación necesita el fork original EasyPlaceFix 0.6.7 para las clases no modificadas; este directorio es fuente de revisión, no un actualizador.
