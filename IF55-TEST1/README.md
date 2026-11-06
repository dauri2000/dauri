# IF55 TEST1 — prueba de materiales

**Experimental. Solo añade las dos comparaciones de materiales de EasyPlaceFix.**
No contiene las correcciones de orientación, colocación ni huecos de Litematica.
Todavía no se ha probado su inicio ni la aplicación de Mixins dentro de Minecraft.

[Descargar el JAR completo de prueba](https://github.com/dauri2000/dauri/raw/refs/heads/codex/litematica-fixes/IF55-TEST1/ViaFabricPlus-Bedrock-IF55-TEST1-MATERIALES.jar)

Mantiene Minecraft Java 26.3 y la base IF54.2 para Bedrock 1.26.60.6. El cambio
se dirige a EasyPlaceFix 0.6.7 con Litematica 0.29.1 y MaLiLib 0.30.2. Reutiliza
la normalización limitada de datos Bedrock que ya trae el addon para buscar y
seleccionar materiales. No modifica las comparaciones estrictas que protegen
los movimientos de inventario ni cambia las opciones de Easy Place.

## Instalación de prueba

1. Cierra Minecraft y conserva una copia de tu addon IF54.
2. Retira el addon Bedrock anterior de `mods` y coloca este JAR. Deja un solo
   addon Bedrock activo.
3. Mantén ViaFabricPlus base, Litematica, MaLiLib, EasyPlaceFix y Fabric API.
   No cambies la versión de Minecraft Java.

Es el mod completo y no requiere un actualizador. En el menú Mods debe aparecer
IF55 TEST1, versión `1.1.1-StackAnvil-InteractionFix55.0-TEST1`. Para volver a IF54,
cierra el juego y sustituye este archivo por la copia que conservaste.

## Verificación y límites

El archivo pasó la comprobación CRC, no contiene entradas duplicadas y conserva
idénticos el núcleo y todos los JAR internos de IF54.2. Únicamente añade dos
Mixins y cambia la versión y su configuración. Las clases nuevas coinciden con
el candidato que tuvo revisión estructural independiente.

Faltan las pruebas de inicio, Mixins, consultas de materiales e inventario en
Minecraft y contra el servidor. Tampoco se ha probado el inicio sin EasyPlaceFix.
El catálogo de todos los bloques y las correcciones de orientación siguen siendo
trabajo pendiente; esta prueba no los da por resueltos.

SHA256 del JAR completo:
`9bf5ff0f9acb87d9d6601195e83777fb694f510c0b7612bcee7e6a35ce1a9d9e`.

Se volvió a descargar el archivo completo desde Git LFS en un almacén nuevo:
216.227.042 bytes y el mismo SHA256. [Recibo de descarga](DESCARGA-VERIFICADA.json).
El acceso web directo estuvo bloqueado por el proxy del entorno; ese recibo
verifica el objeto remoto completo y no una respuesta del enlace web.

El detalle está en [VERIFICACION.json](VERIFICACION.json). Los fuentes Java de
los dos cambios y el generador ASM están en [fuentes](fuentes). Los originales
y sus licencias se conservan dentro del mod. No se distribuye el cliente
Minecraft, mundos ni registros del usuario.
