# Fuentes incrementales de la prueba de materiales

`src` contiene los dos Mixins nuevos. `tools/BuildMaterialMixins.java` emite sus
clases con ASM y ASM-tree 9.9.1 y verifica los destinos y las llamadas del
EasyPlaceFix 0.6.7 aportado. `config-changes.json` registra las dos entradas
añadidas a la configuración de Mixins del addon.

Se reutiliza sin cambios `BedrockMaterialComparison.normalizeCopy` de IF54.2.
El resto del addon y los JAR internos se conserva byte por byte. Las clases
nuevas se emitieron con ASM porque falta el entorno auténtico de Minecraft
26.3 para compilar estos fuentes; la compilación directa, su comparación con
los bytes generados y las pruebas reales de Mixin siguen pendientes.

Este directorio documenta los cambios y no es necesario para instalar el JAR
completo. No contiene un actualizador, bibliotecas propietarias ni sustitutos
de las clases de Minecraft.
