# Borrador de orientación y Easy Place

Este directorio conserva el trabajo de orientación para Minecraft Java 26.3 y el servidor Bedrock 1.26.60.6. **No contiene un mod instalable ni una corrección certificada dentro del juego.** El TEST1 de materiales publicado anteriormente no incluye estos cambios.

La fuente modifica el fork suministrado de EasyPlaceFix 0.6.7. Conserva su licencia MIT y requiere ese binario original para las clases que no se modifican. Los binarios de Minecraft, los mundos, las grabaciones y los registros del usuario quedan fuera de este borrador.

## Cambios propuestos

- Convertir las coordenadas relativas del clic una sola vez y comprobar el soporte y el resultado real de la vista previa.
- Comparar la orientación, el eje, la mitad de losa y el agua antes de enviar una colocación.
- Preparar la rotación sin mover la cámara y conservarla en cada paquete.
- Separar las colocaciones Bedrock en una cola: preparar A, esperar el intervalo normal, usar A y después preparar B.
- Conservar las teclas físicas y retirar solo la rotación que pertenece a la acción terminada.
- Cancelar trabajos antiguos cuando cambian el mundo, el esquema o los ajustes de Easy Place.

## Pruebas realizadas

Son pruebas de componentes y del traductor con entradas construidas. No equivalen a colocar todos los bloques en un servidor.

| Comprobación | Resultado | Alcance |
| --- | --- | --- |
| Métodos de losas y contexto de Minecraft | 57 casos aprobados | Clases reales; mundo simulado explícitamente |
| Conversión de coordenadas de clic | 26 comprobaciones aprobadas | Clics suministrados; sin raycast completo |
| Contexto de rotación proyectado | 79 casos aprobados | Métodos reales; jugador y mundo simulados |
| Máscara de entrada | 256 casos y 256 recorridos del codec aprobados | Solo Shift puede cambiar; Mixin sin aplicar |
| Cola y traductor Bedrock reales | A y B usan su propia orientación | Dos intervalos normales construidos; sin servidor |

El cliente genuino 26.3 y 40 bibliotecas se restauraron y verificaron. La compilación del conjunto sigue bloqueada por el archivo genuino `fabric-loader-0.19.5.jar`. Algunas clases nuevas se compilaron por separado contra Minecraft y Sponge; eso no acredita la aplicación de los Mixins.

## Pendiente antes de una entrega

Faltan la compilación completa, la transformación con Fabric/Mixin y las pruebas de colocación contra el estado autoritativo de BDS 1.26.60.6. También siguen pendientes los pasos intermedios de macetas con plantas, las sustituciones LOOSEN entre bloques distintos y la colocación manual inmediatamente posterior a Easy Place. No se ha completado la verificación física de todas las familias de bloques.

Para el siguiente paso se necesita el Loader instalado por PrismLauncher:

```text
%APPDATA%\PrismLauncher\libraries\net\fabricmc\fabric-loader\0.19.5\fabric-loader-0.19.5.jar
```

Las necesidades adicionales de assets, bibliotecas nativas y servidor se mantienen separadas de este bloqueo de compilación. Los archivos originales no se han sustituido.
