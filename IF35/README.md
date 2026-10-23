# Actualizar ViaFabricPlus Bedrock de IF34 a IF35

[Descargar el actualizador ZIP](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF35/Actualizador-IF34-a-IF35.zip)

El actualizador sin conexión genera el addon IF35 completo utilizando el IF34 original. La descarga ocupa unos 22 MB e incluye todo lo necesario para actualizar el archivo; no descarga código ni usa datos de tu cuenta.

**Minecraft Java 26.3. Conserva ViaFabricPlus base.** El actualizador usa Java 17 o superior; Minecraft y estos mods requieren Java 25 o superior.

1. Descarga `Actualizador-IF34-a-IF35.zip` y extrae su contenido en una carpeta nueva.
2. Cierra Minecraft y abre `ACTUALIZAR-IF35.bat`.
3. Selecciona tu archivo `ViaFabricPlus-Bedrock-IF34.jar` cuando se abra el selector.
4. El archivo `ViaFabricPlus-Bedrock-IF35.jar` aparecerá junto al actualizador. Mueve el IF34 anterior fuera de `mods` y coloca el IF35 en su lugar. Conserva el JAR base de ViaFabricPlus.

El actualizador comprueba el hash exacto del IF34 original y todos los contenidos del IF35 generado. Conserva el original y aborta si la salida ya existe.

## Cambios y estado

- **Escudo normal:** corrige el selector que confundía la lista vacía de diseños con un escudo decorado y mezclaba el modelo Java con la pose Bedrock. Validado mediante código y comprobación de las clases modificadas; falta confirmación gráfica en partida.
- **Rellenado de recetas:** todavía pendiente. Se añade un diagnóstico interno del motivo de rechazo, sin quitar protecciones ni modificar las decisiones del inventario. Tras seleccionar una receta que falle, comparte `logs/latest.log` de la instancia; el registro comienza con `[IF35 recipe-placement]`.
- No se añade ningún laboratorio ni se modifica el mundo.

Las comprobaciones de contenido del actualizador no equivalen a una prueba funcional de Minecraft o del servidor.
