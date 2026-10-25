# Créditos y retorno del End — IF48

El JAR anterior no registra `SHOW_CREDITS`: `BedrockProtocol.registerPackets` cancela al final los paquetes no registrados. Mojang documenta que el cliente debe contestar cuando terminan los créditos para que el servidor reincorpore al jugador. La referencia PowerNukkitX espera esa respuesta antes de teletransportar desde el portal de salida; no es una ejecución del BDS del usuario.

IF48 añade el intercambio: `SHOW_CREDITS(Start)` dirigido al jugador local vivo del End → Java `GAME_EVENT(WIN_GAME, FIRST_TIME=1)` → `CLIENT_COMMAND(PERFORM_RESPAWN)` del cierre de créditos → Bedrock `SHOW_CREDITS(Finished=1)`. La posición y el destino de regreso siguen dependiendo del servidor. No se fabrican cambios de dimensión, recompensas ni coordenadas.

Se guarda la identidad del jugador y del mundo de esa conexión con referencias débiles, para no retener los chunks del End al salir. Se suprimen aperturas duplicadas pendientes y cierres duplicados u obsoletos; un nuevo Start después de terminar permite otro intento. Una muerte real conserva el manejador original de reaparición. La integración añade una llamada de registro y un predicado después de leer el comando; el resto del manejador queda intacto.

## Comprobaciones

- 15 comprobaciones dirigidas ejecutan el helper emitido, el manejador original de comandos con la inserción y los códecs reales de la respuesta. Incluyen sesión normal, muerte, duplicados, cambio de mundo y aislamiento entre conexiones.
- 6 comprobaciones independientes: reversión canónica de las dos inserciones, verificación ASM de todos los métodos, ausencia de referencias fuertes al mundo/jugador y tres escenarios adicionales de estado.
- Los límites de conexión/transporte y el mundo mínimo que aporta identidad/dimensión están declarados en `EndCreditsProbe.java`. No son una simulación de IA, generación, colisiones ni aceptación del servidor.
- No se ejecutó el cliente Minecraft, su pantalla de créditos ni un servidor Bedrock. La prueba confirma el contrato de traducción; el viaje completo sigue pendiente de una sesión real.

## Referencias

- [Mojang: ShowCreditsPacket](https://github.com/Mojang/bedrock-protocol-docs/blob/4fbe153e04e56645addc681259e398b94c60d14f/json/ShowCreditsPacket.json), documentación actual de protocolo 2238, posterior al objetivo 2193. El enum y los códecs del core entregado confirman Start=0 y Finished=1.
- [Cloudburst: códec de ShowCredits](https://github.com/CloudburstMC/Protocol/blob/bcad5e2491367409d0f04d4edf755c17667bcd30/bedrock-codec/src/main/java/org/cloudburstmc/protocol/bedrock/codec/v291/serializer/ShowCreditsSerializer_v291.java): runtime ID unsigned varlong y estado varint.
- [Geyser, JavaGameEventTranslator](https://github.com/GeyserMC/Geyser/blob/036293d698b762516b2893c43a9bd1025ceabd12/core/src/main/java/org/geysermc/geyser/translator/protocol/java/level/JavaGameEventTranslator.java) y [BedrockShowCreditsTranslator](https://github.com/GeyserMC/Geyser/blob/036293d698b762516b2893c43a9bd1025ceabd12/core/src/main/java/org/geysermc/geyser/translator/protocol/bedrock/BedrockShowCreditsTranslator.java): puente inverso para Java 26.3. MCProtocolLib 26.3 confirma los valores del evento. Es evidencia del contrato, no el cliente Mojang ejecutado.
- [PowerNukkitX: ShowCreditsHandler](https://github.com/PowerNukkitX/PowerNukkitX/blob/37caa9e66690f2d327b33d8a72e7621377ab96c5/src/main/java/org/powernukkitx/network/process/handler/ShowCreditsHandler.java): ejemplo de servidor que espera Finished para regresar.
