# Revisión independiente del puente de créditos del End

## Veredicto

La traducción del contrato es apropiada y comprobable sin inventar una posición de regreso: `SHOW_CREDITS Start` del jugador local vivo en el End abre `GAME_EVENT WIN_GAME=1`; la acción Java `PERFORM_RESPAWN` de esa pantalla se convierte en `SHOW_CREDITS Finished` nativo. Después, el servidor elige el destino y ejecuta la transición normal.

Es una corrección concreta de un paquete que el core anterior no manejaba. No constituye una prueba de la generación del End, de los portales de entrada o de todos sus mecanismos.

## Contrato moderno26.3

Se obtuvo y fijó Geyser en el commit `036293d698b762516b2893c43a9bd1025ceabd12`:

- `gradle/libs.versions.toml`: `mcprotocollib="26.3"`, `minecraft="26.3"`.
- `JavaGameEventTranslator.java`: `WIN_GAME FIRST_TIME` se traduce al `SHOW_CREDITS START_CREDITS` Bedrock; `SEEN_BEFORE` se resuelve con Java `PERFORM_RESPAWN`.
- `BedrockShowCreditsTranslator.java`: `END_CREDITS` Bedrock se traduce a Java `PERFORM_RESPAWN`.

La dirección de este proyecto es la inversa de Geyser. Esas dos traducciones modernas corroboran el mismo enlace entre ambos protocolos.

MCProtocolLib se fijó además en el commit `b4fa8b23064bb85d7c7966cc2cd6bf369c9c1405`:

- `MinecraftCodec.java`: `.minecraftVersion("26.3")`, protocolo777.
- `EnterCreditsValue.java`: `SEEN_BEFORE` es0, `FIRST_TIME` es1.
- `ClientboundGameEventPacket.java`: `WIN_GAME` lee esa enumeración desde el float y serializa su ordinal como float.

Se leyó una implementación histórica de `ClientPacketListener.handleGameEvent` y `WinScreen`: WinGame1 abre la pantalla y su callback al terminar o cerrar manda `PERFORM_RESPAWN`. Esa implementación histórica no se usa como prueba del cuerpo exacto del cliente26.3; las referencias26.3 anteriores son la evidencia moderna del contrato. El cliente Mojang completo26.3 sigue sin estar disponible aquí.

Los serializadores Cloudburst ya archivados por el autor confirman los campos nativos: runtimeID unsigned-varlong y estado signed-varint. PNX espera el fin de créditos antes de elegir el spawn/teleport del portal. PNX es una implementación de servidor de referencia y no equivale a BDS.

## Revisión del estado

- El estado vive en cada `UserConnection` y captura mediante `WeakReference` las identidades exactas del jugador y `ChunkTracker` al empezar. Los trackers actuales de la conexión siguen siendo sus propietarios fuertes. El marcador completado no retiene los chunks del End anterior después de salir.
- Los START ajenos, estados desconocidos, el jugador muerto o fuera del End no abren una sesión.
- Un START repetido mientras sigue pendiente no reabre la pantalla. Un **nuevo START explícito después de Finished** rearma la sesión aunque todavía siga en el mismo mundo. Este último caso se señaló durante la revisión y se corrigió antes de congelar los hashes.
- Con sesión pendiente y jugador vivo, el comando de la pantalla queda cancelado: no se envían los campos del respawn de muerte. Solo una sesión todavía válida envía Finished y solo una vez.
- Una pantalla vieja después de cambiar de mundo/jugador no puede confirmar la sesión anterior ni disparar un falso respawn de muerte.
- Si el jugador está realmente muerto, el helper quita su marcador y deja actuar al handler original de respawn. La muerte conserva prioridad incluso si la pantalla era vieja y cambió el mundo.
- El helper no cambia health, posición, dimensión, inventario, atributos o destino. El guard End es un alcance deliberado: este parche no traduce créditos solicitados por plugins fuera del End.

Una acción Java `PERFORM_RESPAWN` no contiene un identificador de pantalla; el puente distingue el uso por el estado pendiente y el health actual del core. No se afirma que todos los órdenes posibles de una pantalla externa o de un plugin estén reproducidos en la partida real.

## Verificación independiente

Se revisaron y ejecutaron los bytes actuales, con `-Xverify:all`:

| Clase | SHA256 |
| --- | --- |
| `packet/ClientPlayerPackets.class` | `c833b164912b1d4cac4dc2a5f1449ce2cbfe8f353069b198023efedd63b599ae` |
| `packet/EndCreditsBridge.class` | `a7ae969f2b55691cfac4c46a277fd84b087c534ce9b37c47a3645f1e5389738c` |

Rutas relativas a `net/raphimc/viabedrock/protocol/` dentro del core.

Seis comprobaciones independientes (tres grupos estructurales y tres escenarios de comportamiento):

1. Todos los métodos del `ClientPlayerPackets` emitido pasan ASM BasicVerifier.
2. Al retirar exactamente la nueva llamada de registro y el hook de comando, las instrucciones, debug, anotaciones y demás contenido original vuelven a coincidir. Se normalizan frames/maxima porque el generador los recalcula. La nueva etiqueta de fallthrough se fusiona con la etiqueta original de línea573 y debe conservarse como ese ancla al revertir.
3. Los campos de contexto `player` y `world` son `final WeakReference` y la clase no contiene ningún campo fuerte de tipo `ClientPlayerEntity`/`ChunkTracker`. Es una comprobación estructural de retención, sin depender de un GC forzado.
4. Nuevo START después de Finished en el mismo mundo vuelve a abrir créditos.
5. Mundo cambiado más health0 conserva el respawn original y elimina el marcador, sin Finished.
6. START con health0 no crea una sesión.

Las pruebas de comportamiento reutilizan únicamente el bootstrap/transporte/frontera de mundo del harness del autor; ejecutan el helper y el handler real emitidos. El `ChunkTracker` de prueba solo aporta identidad y dimensión, sin ejecutar generación/carga. No se ejecutó ninguna GUI real, teleport de regreso ni servidor BDS.

Las fuentes remotas completas guardadas en esta carpeta son referencias de análisis. Para el ZIP distribuido bastan el informe, manifiestos de URLs/hashes, nuestros verificadores y logs; no incorporar fuentes completas de terceros ni clases compiladas de pruebas.
