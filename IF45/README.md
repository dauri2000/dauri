# ViaFabricPlus Bedrock IF45

[Descargar el mod completo IF45](https://github.com/dauri2000/dauri/raw/refs/heads/codex/if35-download/IF45/ViaFabricPlus-Bedrock-IF45.jar)

JAR completo para sustituir el addon Bedrock IF44. Conserva las correcciones acumuladas anteriores. El usuario confirmó que el vídeo del 25 de octubre estaba grabado con IF44.

## Qué cambia

**Confirmación de colocaciones.** IF44 podía confirmar una colocación de bloque tras recibir solo una actualización de la capa secundaria de Bedrock. Se reprodujo con el manejador real: la capa principal seguía siendo aire, pero el mod enviaba la confirmación a Java. IF45 exige una actualización de la capa principal para esas predicciones de bloque. Los cubos siguen admitiendo las actualizaciones de agua de la capa secundaria. Se conserva el orden de las confirmaciones y el tiempo de espera anterior.

**Clics de construcción modificados.** La colocación flexible puede enviar a Java un clic sobre una celda de aire: Java predice el bloque allí, mientras que Bedrock exige un soporte real. Antes de predecir y enviar, IF45 recupera el hit físico del cliente cuando llega exactamente a esa misma celda y el soporte pertenece a una lista conservadora de clases normales de construcción, como piedra, vidrio o escaleras. Así Java y Bedrock usan el mismo destino. También restaura un hit físico equivalente cuando el cursor contiene coordenadas especiales del protocolo Java de colocación precisa, con el mismo soporte y cara.

La corrección no busca un vecino arbitrario, reintenta clics ni inventa bloques o consumos. Un bloque que Java ya predijo sólido queda intacto: eso evita alterar las cadenas normales mientras la copia Bedrock todavía espera su actualización. Se excluyen contenedores, puertas, losas combinables y clases desconocidas de la adaptación del hit. La colocación recuperada usa la cara física normal; no fuerza en Bedrock las orientaciones especiales de un protocolo Java.

La vista previa blanca y azul del vídeo coincide con el renderer de colocación flexible de Tweakeroo. La revisión utilizó una fuente de Tweakeroo para 26.3; no capturó los paquetes de la sesión del vídeo y no demuestra que cada intento desaparecido tuviera esa causa. La adaptación no necesita Tweakeroo instalado ni modifica su configuración.

## Instalación

1. Cierra Minecraft por completo.
2. Quita de la carpeta `mods` el addon `ViaFabricPlus-Bedrock-IF44.jar` o cualquier otro addon Bedrock IF anterior. Conserva el ViaFabricPlus base y tus demás mods.
3. Copia `ViaFabricPlus-Bedrock-IF45.jar` en esa carpeta. Debe quedar **un solo addon Bedrock IF**.
4. Abre Minecraft y comprueba que Mods muestra **IF45**, versión `1.1.1-StackAnvil-InteractionFix45.1`.
5. Entra al servidor y prueba primero una fila de bloques contra una pared normal; después prueba la colocación rápida y flexible que fallaba.

Es el mod completo; no hay actualizador ni hay que modificar el JAR anterior.

## Verificación y límites

Los informes adjuntos fijan los hashes de las clases probadas y el JAR final. Las comprobaciones usan los manejadores de protocolo y modelos reales, pruebas de la política del cliente y revisión estructural del hook. El parche añadido al mixin conserva de forma canónica el método anterior de los cubos; las clases de selección de agua de IF44 y los otros fixes protegidos se comparan por hash.

No se ejecutó un cliente Minecraft transformado ni un servidor Bedrock real en este entorno. Las pruebas reproducen los defectos descritos; no garantizan que todo rechazo de colocación del servidor tenga la misma causa. Si queda algún caso, hará falta el `latest.log` de una prueba con IF45 para seguir esa sesión concreta.
