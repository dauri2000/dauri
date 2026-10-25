# IF47 — diagnóstico temporal de experiencia

[Descargar el JAR completo de diagnóstico XP](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF47/ViaFabricPlus-Bedrock-IF47-XP-Diagnostico.jar)

Esta versión sirve para registrar el problema de las bolitas de XP que permanecen cerca del jugador. **La causa y la corrección de recogida siguen pendientes.** Conserva IF46 y las correcciones anteriores. Es el mod completo, sin actualizador.

## Prueba

1. Cierra Minecraft por completo.
2. Sustituye el addon Bedrock IF anterior de `mods` por `ViaFabricPlus-Bedrock-IF47-XP-Diagnostico.jar`. Conserva ViaFabricPlus base y deja un solo addon Bedrock IF.
3. Abre Minecraft y confirma **IF47 XP Diagnóstico**, versión `1.1.1-StackAnvil-XPDiagnostics47.1`.
4. Entra al servidor y reproduce enseguida el problema junto a los orbes. Muévete a su alrededor como en el vídeo y espera unos **45 segundos**.
5. Cierra Minecraft y adjunta **`latest.log`**, de la carpeta `logs` de esa instancia. En PrismLauncher puedes abrir la carpeta de Minecraft de la instancia y entrar en `logs`. No hace falta otro vídeo.

El registro empieza con el primer orbe observado a 32 bloques o menos de la posición traducida del jugador. Hay una ventana por conexión: para repetir la prueba, sal del servidor y vuelve a entrar. Si la ventana comenzó varios minutos antes de reproducir el fallo, vuelve a conectar y haz la prueba enseguida.

## Qué registra

Las líneas llevan el prefijo `[IF47-XP]` y contienen los identificadores de los orbes, su última posición recibida, la antigüedad de esa posición, los vectores de movimiento recibidos, las retiradas observadas y los atributos de experiencia guardados en el modelo del puente.

Se limita a cuatro orbes simultáneos, 32 identidades durante la ventana, dos muestras por segundo en total y un máximo de 500 líneas. La ventana dura 45 segundos y se cierra en el siguiente callback; no crea otro temporizador. Las posiciones y las retiradas se observan dentro de las rutas existentes. El observador conserva los paquetes y el estado de juego de IF46.

## Límites de interpretación

La posición recibida de un orbe puede ser antigua mientras llegan vectores de movimiento. No equivale a su posición actual en pantalla ni demuestra por sí sola su posición actual en el servidor. La posición del jugador es la traducida por el puente, no una confirmación de posición aceptada por el servidor. Los atributos de XP son una caché que inicialmente puede contener cero; su presencia tampoco certifica una actualización reciente del servidor.

Una retirada observada no certifica por sí sola que se haya cobrado experiencia: también puede ocurrir durante una limpieza del mundo. La ausencia de motion hints no demuestra que el spawn no incluyera velocidad.

Las pruebas usan handlers, entidades y paquetes reales con fronteras explícitas de conexión, reloj y logger. Se comprobó la conservación del contenido y orden de paquetes y la reversión canónica de los dos cambios de llamadas. **No se ejecutó Minecraft ni un servidor Bedrock real**, ni se presenta esta entrega como una corrección de XP.
