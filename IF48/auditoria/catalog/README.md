# Catálogo de comprobación: stronghold y End

Este archivo define el alcance de una revisión, **no certifica que el addon ya funcione ni que se haya ejecutado un combate**. Cada fila identifica qué debe comprobar el auditor. El resultado real debe cruzarse con sus pruebas de código/paquetes y con una sesión Bedrock cuando sea necesaria.

El alcance es el stronghold, el End vanilla, el combate del dragón, ciudades/barcos, sus materiales/botines y las rutas compartidas necesarias para viajar y combatir. Las variantes de generación dependen de la versión y del servidor. Todos los objetos del juego pueden llevarse al End; no se repite aquí el registro completo del Overworld como si fueran contenido propio del End.

## Versiones y fuentes

- Cliente indicado por el proyecto: Java 26.3. El puente declara compatibilidad Bedrock 1.26.51, protocolo 2193, según la inspección del agente principal; eso no identifica la versión real del servidor.
- Fuente oficial: [Mojang/bedrock-samples](https://github.com/Mojang/bedrock-samples/tree/46ba6ea985fb5a92d79a9419198f10dda14c199d), commit `46ba6ea985fb5a92d79a9419198f10dda14c199d`. Su `version.json` declara `1.26.50.4`, del 15-09-2026. Es una referencia próxima al objetivo del puente y no una extracción del servidor del usuario.
- Se descargaron entidades End, silverfish, reglas de enderman, fruta de chorus, tablas de botín End/stronghold y textos oficiales de ayuda. `reference/sources*.json` conserva URL, tamaño, SHA256 y errores 404. Un 404 de muestra no demuestra que la mecánica no exista.
- Los textos oficiales confirman acceso desde stronghold, relación dragón/cristales/aliento, resurrección con cuatro cristales, gateways, obtención/planeo/reparación de élitros, cohetes, ender chests y conservación del inventario de shulkers.
- Auxiliar: [plantillas Java 26.3 extraídas](https://github.com/InventivetalentDev/minecraft-assets/tree/d40ae2d4a2df95caf42e4af0f6f60f0aa926651f/data/minecraft/structure/end_city), commit `d40ae2d4a2df95caf42e4af0f6f60f0aa926651f`. Se inspeccionaron 20 NBT y sus paletas. Sirven para no olvidar materiales y marcadores; **no prueban la generación ni el diseño exacto de Bedrock**. Los `structure_block` de modo DATA son marcadores de generación, no bloques que deban aparecer al jugador. La plantilla del barco incluye 2 pociones Java `strong_healing`; su contenido Bedrock exacto debe comprobarse en el servidor.
- Minecraft Wiki y Microsoft Learn no pudieron leerse desde este entorno (proxy 403). No se presentan como fuentes consultadas.

## Diferencias y límites que deben conservarse

- El servidor Bedrock decide generación, IA, daño, recompensas, reglas de botín, teleport y activación de portales. El cliente traduce su estado y las acciones; no debe sustituirlos por supuestos de Java.
- La fortaleza Bedrock puede variar en conectividad y sala del portal; no debe imponerse el patrón de generación de Java. Tampoco se copia el catálogo de biomas End de Java como si fuera el registro nativo Bedrock.
- El botín oficial actual incluye `diamond_spear` y `copper_horse_armor`; omitirlos por usar una lista de 1.21 dejaría huecos. El stronghold también incluye `record_otherside` y la plantilla `eye`; End city incluye `spire`. El servidor puede usar otra versión o modificar tablas.
- El arco sin flecha visible es un fallo actual bajo investigación. La recogida lenta de XP sigue sin resolver; IF47 solo la registra. La antigua incidencia visual de mapas tampoco se declara resuelta. Ningún catálogo anula esas incidencias.
- La barra de nivel no mide por sí sola la recogida de XP: Mending puede reparar equipo. `REMOVE` de un actor tampoco prueba por sí solo una recompensa.
- El catálogo cubre todos los tipos de entrada de las cuatro tablas oficiales descargadas, una fila por entrada no vacía, y no afirma que sus probabilidades o cantidades se hayan probado dentro del juego.

## Lista de revisión

### stronghold_estructura (15)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_estructura-001 | Localización con ojos de ender | Lanzamiento, entidad señal, dirección y posición recibidas; recogida/rotura decidida por Bedrock. |
| stronghold_estructura-002 | Acceso y escaleras de entrada | Geometría, colisión, altura y conectividad de los chunks enviados. |
| stronghold_estructura-003 | Pasillos rectos y giros | Bloques sólidos, puertas y paso del jugador sin predicción a través de paredes. |
| stronghold_estructura-004 | Intersecciones y habitaciones de conexión | Carga completa y continuidad de conexiones; generación depende del servidor. |
| stronghold_estructura-005 | Escaleras internas y escaleras de caracol | Colisión de escalones, orientación y desplazamiento. |
| stronghold_estructura-006 | Habitación de fuente | Fuente, agua, colisión, cubo y actualizaciones de líquido. |
| stronghold_estructura-007 | Almacén con cofre | Plataforma, acceso, cofre, saqueo y sincronización del inventario. |
| stronghold_estructura-008 | Celdas | Barrotes, puertas, botones y colisión de bloques conectados. |
| stronghold_estructura-009 | Biblioteca de una o dos plantas | Estanterías, plataformas, barandillas, escaleras, telarañas y cofres. |
| stronghold_estructura-010 | Sala del portal | Marcos orientados, escalera, lava, barrotes y generador de silverfish. |
| stronghold_estructura-011 | Cofres de pasillo | Tabla stronghold_corridor: apertura, nombres, NBT/componentes, transferencia y cierre. |
| stronghold_estructura-012 | Cofres de cruce/almacén | Tabla stronghold_crossing: mismos controles de inventario; contenido generado por el servidor. |
| stronghold_estructura-013 | Cofres de biblioteca | Tabla stronghold_library: libros encantados, mapa, brújula y plantilla. |
| stronghold_estructura-014 | Intersección con cuevas, agua u otras estructuras | Enviar el terreno real recibido; no reconstruir un stronghold ideal en el cliente. |
| stronghold_estructura-015 | Variación de generación de Bedrock | No aplicar como requisito el patrón de anillos ni la conectividad garantizada de Java; no prometer sala del portal en toda estructura. |

### stronghold_mecanica (16)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_mecanica-001 | Ojo de ender en el aire | Usar ítem, consumir cuando corresponda, entidad eye_of_ender_signal, partículas y resultado de drop. |
| stronghold_mecanica-002 | Inserción de un ojo en un marco | Cara/posición tocada, estado del ojo, reducción de pila y sonido. |
| stronghold_mecanica-003 | Activación del portal completo | Doce marcos válidos y sus orientaciones; rellenar interior desde actualizaciones nativas. |
| stronghold_mecanica-004 | Entrada al portal del End | Contacto, cambio de dimensión, posición de llegada y confirmaciones. |
| stronghold_mecanica-005 | Rotura de bloques infestados | Mostrar variante correcta, enviar rotura real y aceptar aparición de silverfish. |
| stronghold_mecanica-006 | Despertar silverfish cercanos | El servidor decide la infestación y las entidades; mantener bloques y entidades coherentes. |
| stronghold_mecanica-007 | Generador de silverfish | Spawner, datos de bloque-entidad, aparición/desaparición, daño y eliminación del generador. |
| stronghold_mecanica-008 | Puertas de madera | Abrir/cerrar, mitades, orientación y colisión. |
| stronghold_mecanica-009 | Puertas de hierro y botones | Interacción, señal, sonido, estados abiertos y restauración. |
| stronghold_mecanica-010 | Barrotes conectados | Conexiones y colisión al pasar o atacar a través. |
| stronghold_mecanica-011 | Escaleras de mano | Adhesión, ascenso/descenso y estado de dirección. |
| stronghold_mecanica-012 | Telarañas | Ralentización, colisión y rotura con herramienta. |
| stronghold_mecanica-013 | Lava de la sala del portal | Renderizado, colisión/daño nativo, fuego y uso de cubos. |
| stronghold_mecanica-014 | Agua y cubos en la fortaleza | Tomar/colocar fuente y sincronizar ítem y capas de fluidos; incluir agua contenida en bloques. |
| stronghold_mecanica-015 | Cofres y saqueo rápido | Clic/shift-clic, apilado, encantamientos/componentes y recuentos confirmados. |
| stronghold_mecanica-016 | Mobs normales del Overworld que entren | Ruta genérica de aparición, daño y eliminación; no son especies exclusivas del stronghold. |

### stronghold_bloque (27)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_bloque-001 | Ladrillos de piedra | Textura, solidez, rotura y drop. |
| stronghold_bloque-002 | Ladrillos de piedra musgosos | Variante y drop conservados. |
| stronghold_bloque-003 | Ladrillos de piedra agrietados | Variante y drop conservados. |
| stronghold_bloque-004 | Ladrillos de piedra cincelados | Cobertura de variante si la plantilla/versión los utiliza; no prueba de que toda fortaleza los genere. |
| stronghold_bloque-005 | Piedra infestada | Apariencia y rotura/entidad nativas. |
| stronghold_bloque-006 | Ladrillos de piedra infestados | Conservar variante infestada en la interacción y aceptar spawn nativo. |
| stronghold_bloque-007 | Ladrillos musgosos/agrietados/cincelados infestados | Cobertura de las tres variantes auxiliares; no afirmar distribución exacta sin plantilla Bedrock. |
| stronghold_bloque-008 | Escaleras de ladrillos de piedra | Direcciones, mitad, esquinas, colisión y fluido. |
| stronghold_bloque-009 | Losas de piedra y de ladrillos de piedra | Mitad inferior/superior/doble y colisión; presencia exacta depende de la plantilla. |
| stronghold_bloque-010 | Adoquín | Textura, solidez y actualización. |
| stronghold_bloque-011 | Tablones de roble | Solidez, rotura y colocación. |
| stronghold_bloque-012 | Vallas de roble | Conexiones, colisión y antorchas sobre la valla. |
| stronghold_bloque-013 | Puerta de roble | Dos mitades, bisagra, facing y open. |
| stronghold_bloque-014 | Puerta de hierro | Dos mitades, facing y señal. |
| stronghold_bloque-015 | Botón de piedra | Cara, estado pulsado, señal y restauración. |
| stronghold_bloque-016 | Barrotes de hierro | Conexión y colisión. |
| stronghold_bloque-017 | Antorcha de pie y en pared | Cara, luz y soporte. |
| stronghold_bloque-018 | Estantería | Modelo, rotura, libros y posible toque de seda. |
| stronghold_bloque-019 | Telaraña | Modelo y ralentización. |
| stronghold_bloque-020 | Escalera de mano | Facing y escalada. |
| stronghold_bloque-021 | Cofre simple | Facing, apertura, inventario y bloque-entidad. |
| stronghold_bloque-022 | Agua fuente/corriente | Renderizado, fuente real, flujo nativo y cubo. |
| stronghold_bloque-023 | Lava fuente/corriente | Renderizado, flujo nativo, daño y cubo. |
| stronghold_bloque-024 | Generador de criaturas (silverfish) | Tipo de entidad y parámetros enviados; aparición y rotura. |
| stronghold_bloque-025 | Marco del portal del End sin ojo/con ojo | Facing, eye, modelo y activación. |
| stronghold_bloque-026 | Bloque de portal del End | Plano, renderizado y transición. |
| stronghold_bloque-027 | Aire y terreno vecino recibido | Ausencia de colisión fantasma y carga de chunks; no limitar el catálogo de cuevas a materiales de la fortaleza. |

### end_estructura (10)

| ID | Elemento | Comprobar |
|---|---|---|
| end_estructura-001 | Plataforma de entrada de obsidiana | Posición de llegada, entorno y regeneración nativa al volver a entrar. |
| end_estructura-002 | Isla principal | Chunks, end stone, límites del terreno y colisión. |
| end_estructura-003 | Pilares de obsidiana | Geometría, cristales, fuego y bloques base. |
| end_estructura-004 | Jaulas de barrotes de los cristales | Conexiones/colisión y ataques a través de huecos. |
| end_estructura-005 | Fuente/portal de salida central | Bedrock, antorchas, portal activo/inactivo, huevo y retorno. |
| end_estructura-006 | Portales de enlace de la isla central | Bloque-entidad, animación/haz, contacto/perla, destino y retorno. |
| end_estructura-007 | Islas exteriores y pequeños islotes | Chunks, vacío entre islas, crecimiento de chorus y colisión. |
| end_estructura-008 | Ciudades del End | Torres, habitaciones, puentes, pasarelas, cofres, ender chests y shulkers. |
| end_estructura-009 | Barcos del End | Acceso, shulkers, cabeza, botín, soporte de pociones y marco de élitros. |
| end_estructura-010 | Portales de enlace en islas exteriores | Destino real del bloque-entidad y traslado seguro recibido; no inventar coordenadas. |

### end_mecanica (26)

| ID | Elemento | Comprobar |
|---|---|---|
| end_mecanica-001 | Dragón: aparición y seguimiento | ADD/MOVE/MOTION/REMOVE, dimensiones y orientación visibles coherentes. |
| end_mecanica-002 | Dragón: vida y barra de jefe | Vida recibida y eventos de barra sin barra permanente o duplicada. |
| end_mecanica-003 | Dragón: partes del cuerpo y ataques | Resolver hitbox/ID de cada parte al actor Bedrock correcto. |
| end_mecanica-004 | Dragón: vuelo, giros y embestida | Movimiento interpolado y daño/impulso nativo. |
| end_mecanica-005 | Dragón: aterrizaje, posado y despegue | Fases y animaciones traducidas; no imponer IA Java. |
| end_mecanica-006 | Dragón: ataque cuerpo a cuerpo | Alcance, ID de parte, daño confirmado y feedback. |
| end_mecanica-007 | Dragón: flechas/proyectiles en vuelo o posado | Colisión visible y resultado nativo; inmunidades/deflexión pertenecen a Bedrock. |
| end_mecanica-008 | Dragón: bolas de fuego | Aparición, velocidad, trayectoria, impacto y eliminación. |
| end_mecanica-009 | Dragón: nube de aliento | Radio, partículas, duración, daño y fuente de pociones. |
| end_mecanica-010 | Dragón: recoger aliento con frasco | Interacción de objeto, resultado dragon_breath y modificación nativa de la nube. |
| end_mecanica-011 | Cristales: curación y haz | Beam target, posición, base visual y vida real del dragón. |
| end_mecanica-012 | Cristales: destrucción y explosión | Actor correcto, explosión/sonido/daño/bloques y retirada. |
| end_mecanica-013 | Dragón: rotura de bloques | Aceptar destrucción y excepciones nativas; no mantener fantasmas. |
| end_mecanica-014 | Dragón: muerte | Fase/animación, desaparición, sonidos, portal y huevo. |
| end_mecanica-015 | Dragón: experiencia de la muerte | XP nativa, movimiento/recogida de orbes y nivel/equipo; fallo de recogida conocido todavía pendiente. |
| end_mecanica-016 | Dragón: resurrección con cuatro cristales | Validación de posiciones y secuencia nativas, pilares/cristales y nuevo dragón. |
| end_mecanica-017 | Dragón: muertes posteriores y recompensas | Aceptar XP/huevo/gateway según reglas Bedrock; no copiar cantidades o límites de Java. |
| end_mecanica-018 | Enderman: mirada y calabaza tallada | Aggro del servidor, metadata/animación y casco recibido. |
| end_mecanica-019 | Enderman: combate, agua y teletransporte | Daño, evasión de proyectiles, traslado y bloques transportados. |
| end_mecanica-020 | Enderman: drops y experiencia | Perlas/pilas, recogida de ítems separada de orbes XP. |
| end_mecanica-021 | Shulker: unión a superficies, apertura y armadura | Cara adherida, altura/peek, colisión y daño nativo. |
| end_mecanica-022 | Shulker: teletransporte y reproducción cuando proceda | Movimiento/spawns confirmados; comportamiento exacto de versión/gamerules pertenece a Bedrock. |
| end_mecanica-023 | Shulker: proyectiles perseguidores | Trayectoria, objetivo, impacto, bloqueo/rotura y retirada. |
| end_mecanica-024 | Shulker: levitación y caída posterior | Efecto recibido, duración visible, movimiento y daño de caída. |
| end_mecanica-025 | Shulker: caparazones y XP | Botín/looting, recogida y ausencia de pérdida por desincronización. |
| end_mecanica-026 | Endermite surgido por perla | Aparición, objetivo, vida útil y desaparición recibidos. |

### end_entidad (15)

| ID | Elemento | Comprobar |
|---|---|---|
| end_entidad-001 | minecraft:ender_dragon | Actor principal, partes, fase, vida, bossbar, muerte. |
| end_entidad-002 | minecraft:ender_crystal | Actor interactuable, haz, base y explosión. |
| end_entidad-003 | minecraft:dragon_fireball | Movimiento, impacto y retirada. |
| end_entidad-004 | minecraft:area_effect_cloud | Radio, partículas, duración y efectos nativos. |
| end_entidad-005 | minecraft:enderman | Modelo, metadata, ataque, bloque cargado y teletransporte. |
| end_entidad-006 | minecraft:shulker | Cara adherida, apertura, color y teletransporte. |
| end_entidad-007 | minecraft:shulker_bullet | Homing, golpe y levitación. |
| end_entidad-008 | minecraft:endermite | Spawn de perla, daño y eliminación. |
| end_entidad-009 | minecraft:xp_orb | Vida visual y recogida; pendiente real ya reportado por el usuario. |
| end_entidad-010 | minecraft:item | Botín, cantidad y recogida nativa. |
| end_entidad-011 | minecraft:ender_pearl | Proyectil y teletransporte del propietario. |
| end_entidad-012 | minecraft:arrow | Flechas disparadas e impacto; distinguirlas del modelo de flecha sobre el arco. |
| end_entidad-013 | Minecraft:marco de objeto (representación Bedrock de bloque) | Élitros visibles, rotación, ataque/interacción y obtención del objeto. |
| end_entidad-014 | minecraft:silverfish | Stronghold: spawn/infestación/daño y retirada. |
| end_entidad-015 | minecraft:eye_of_ender_signal | Stronghold: señal, movimiento y final del lanzamiento. |

### end_bloque (26)

| ID | Elemento | Comprobar |
|---|---|---|
| end_bloque-001 | Piedra del End | Solidez, textura y drop. |
| end_bloque-002 | Obsidiana | Pilares/plataforma/barco, rotura y regeneración nativa. |
| end_bloque-003 | Roca madre | Fuente, gateways y base de cristales; colisión e indestructibilidad. |
| end_bloque-004 | Barrotes de hierro | Jaulas de cristales: conexiones, colisión y rotura. |
| end_bloque-005 | Fuego | Plano/renderizado, interacción y daño nativo. |
| end_bloque-006 | Antorchas de pie/en pared | Fuente de salida, facing, luz y soporte. |
| end_bloque-007 | Portal del End | Renderizado y salida/entrada de dimensión. |
| end_bloque-008 | Portal de enlace del End | Bloque-entidad, haz y destino. |
| end_bloque-009 | Huevo de dragón | Modelo, soporte, caída, teletransporte y recogida confirmada. |
| end_bloque-010 | Planta de chorus | Conexiones, soporte, rotura en cadena y fruto. |
| end_bloque-011 | Flor de chorus | Edad, crecimiento nativo, proyectiles/rotura y drop. |
| end_bloque-012 | Ladrillos de piedra del End | Ciudad/barco: textura, solidez y drop. |
| end_bloque-013 | Bloque de purpur | Textura, solidez y drop. |
| end_bloque-014 | Pilar de purpur | Ejes X/Y/Z y textura. |
| end_bloque-015 | Escaleras de purpur | Facing, mitades, esquinas y colisión. |
| end_bloque-016 | Losa de purpur | Inferior/superior/doble y colisión. |
| end_bloque-017 | Vara del End | Todas las direcciones, luz y colisión. |
| end_bloque-018 | Cristal tintado de magenta | Transparencia y colisión. |
| end_bloque-019 | Estandarte magenta de pared | Color, facing y bloque-entidad; corroborado en paletas Java, generación Bedrock exacta pendiente. |
| end_bloque-020 | Escalera de mano | Orientación y ascenso en torres/barco. |
| end_bloque-021 | Cofre | Loot de ciudad/barco, apertura, transferencia y contenido. |
| end_bloque-022 | Cofre de ender | Apertura e inventario privado persistente entre dimensiones. |
| end_bloque-023 | Soporte para pociones | Modelo, botellas/slots y saqueo de pociones. |
| end_bloque-024 | Cabeza de dragón de suelo/pared | Orientación, mandíbula por animación/señal y objeto al recoger. |
| end_bloque-025 | Marco de objeto con élitros | Ítem mostrado/rotación, interacción y obtención. |
| end_bloque-026 | Aire y vacío | Nada sólido inventado en huecos, límite del mundo y daño/respawn nativos. |

### end_objeto_mecanica (25)

| ID | Elemento | Comprobar |
|---|---|---|
| end_objeto_mecanica-001 | Arco: animación al tensar y flecha apoyada | Modelo de etapas y flecha visible; fallo actual reportado, auditor bow_render_audit. |
| end_objeto_mecanica-002 | Arco: disparo | Uso/soltado, consumo y tipo de flecha, durabilidad y proyectil nativo. |
| end_objeto_mecanica-003 | Ballesta normal/multidisparo/perforación | Carga, componente de proyectiles, disparo y consumo confirmados. |
| end_objeto_mecanica-004 | Flechas normales y con efecto | Tipo/poción, trayectoria, impacto y recogida; no inventar espectrales nativas de Bedrock. |
| end_objeto_mecanica-005 | Armas de cuerpo a cuerpo | Ataques al actor/parte, durabilidad, encantamientos y resultado nativo. |
| end_objeto_mecanica-006 | Escudo | Activación traducida, bloqueo/daño y animación; preservar corrección previa. |
| end_objeto_mecanica-007 | Armadura y encantamientos | Equipamiento, protección y desgaste decididos por Bedrock. |
| end_objeto_mecanica-008 | Tótem de inmortalidad | Mano/equipamiento, consumo y evento de resurrección. |
| end_objeto_mecanica-009 | Perla de ender | Uso/cooldown, trayectoria, daño de teletransporte y correcciones de posición. |
| end_objeto_mecanica-010 | Ojo de ender | Lanzamiento, señal y colocación en marco. |
| end_objeto_mecanica-011 | Cristal del End | Colocación sobre soporte permitido y actor resultante; resurrección gestionada por servidor. |
| end_objeto_mecanica-012 | Frasco de cristal y aliento de dragón | Conversión al recoger nube y cantidades confirmadas. |
| end_objeto_mecanica-013 | Poción de aliento/prolongada derivada | NBT/componentes, iconos y recetas/resultados del servidor. |
| end_objeto_mecanica-014 | Fruta de chorus | Comer incluso sin hambre, cooldown y teletransporte confirmado. |
| end_objeto_mecanica-015 | Fruta de chorus horneada | Resultado de horno y recetas de purpur/varas. |
| end_objeto_mecanica-016 | Caparazón de shulker | Botín, apilado y receta de caja. |
| end_objeto_mecanica-017 | Caja de shulker sin teñir y dieciséis colores | Inventario de 27 slots, abrir/cerrar, contenido anidado y conservación al romper; no guardar otra shulker dentro. |
| end_objeto_mecanica-018 | Élitros | Obtener del marco, equipar, iniciar/parar planeo, durabilidad y reparación. |
| end_objeto_mecanica-019 | Cohete de fuegos artificiales | Impulso durante planeo, consumo y daño cuando tiene explosiones. |
| end_objeto_mecanica-020 | Cabeza de dragón como ítem/equipo | Inventario, colocación, equipo y modelo. |
| end_objeto_mecanica-021 | Huevo de dragón como ítem | Recogida especial, transporte, colocación y recuento nativo. |
| end_objeto_mecanica-022 | Pociones del barco | Poción de curación/variante de la versión, botella, consumo y efecto. |
| end_objeto_mecanica-023 | Plantilla de armadura espiral (spire) | Ciudad: nombre/icono, apilado y conservación de NBT. |
| end_objeto_mecanica-024 | Plantilla de armadura ojo (eye) | Stronghold: nombre/icono, apilado y conservación de NBT. |
| end_objeto_mecanica-025 | Equipo encantado del botín | Conservar encantamientos, atributos/durabilidad y coste de reparación; yunque es una ruta compartida. |

### end_mecanica_compartida (22)

| ID | Elemento | Comprobar |
|---|---|---|
| end_mecanica_compartida-001 | Cambio Overworld → End | Identidad de dimensión, registry, carga de chunks, posición y confirmación de teleport. |
| end_mecanica_compartida-002 | Cambio End → Overworld por salida | Evento de fin, créditos si corresponde y punto de reaparición recibido. |
| end_mecanica_compartida-003 | Reentrada y desconexión en el End | Reconexión, respawn, limpieza de actores/chunks y posición inicial. |
| end_mecanica_compartida-004 | Traslado por gateway | Pequeño portal, perla u otro acceso válido nativo, cooldown y chunks destino. |
| end_mecanica_compartida-005 | Muerte, inventario y reaparición | No inventar respawn en el End; aceptar reglas y posición del servidor. |
| end_mecanica_compartida-006 | Caída al vacío | Altura/daño, salud, totem si procede, muerte y pérdida de equipo según Bedrock. |
| end_mecanica_compartida-007 | Colocación rápida y puentes | No perder bloques ni dejar fantasmas; preservar IF45, resultado live anterior pendiente. |
| end_mecanica_compartida-008 | Rotura y recogida de bloques | Confirmación de estado, partículas, drops e inventario. |
| end_mecanica_compartida-009 | Agua, cubos y bloques con agua | Mecánica permitida en End, fuente real, capas y sincronización; preservar IF44. |
| end_mecanica_compartida-010 | Camas usadas en End | Explosión/sonido/bloques/daño según servidor; no establecer respawn. |
| end_mecanica_compartida-011 | Ancla de reaparición usada en End | Carga y explosión según reglas Bedrock; no asumir función válida de Nether. |
| end_mecanica_compartida-012 | Portales del Nether construidos en End | Aceptar que la dimensión no habilita la ruta normal al Nether; no crear un portal cliente ficticio. |
| end_mecanica_compartida-013 | Brújula, reloj y mapas en End | Presentación especial por dimensión; mapas ya tenían fallo visual reportado y no se declaran resueltos. |
| end_mecanica_compartida-014 | Iluminación, cielo, niebla y música | Valores y eventos de dimensión; el End no usa el ciclo visual normal del Overworld. |
| end_mecanica_compartida-015 | Lluvia y tormenta | No aplicar clima del Overworld a End por error; estados conservados al cambiar dimensión. |
| end_mecanica_compartida-016 | Efectos, salud, hambre y caída | Paquetes de atributos/efectos, duración visible y movimiento; daño autoritativo nativo. |
| end_mecanica_compartida-017 | Sonidos, partículas y eventos de nivel | Dragón/cristales/shulkers/portales/chorus: nombre, evento y posición. |
| end_mecanica_compartida-018 | Cofres/ender chests/shulkers con clic rápido | Slots, revisiones, contenido confirmado, apilado y cierre. |
| end_mecanica_compartida-019 | Experiencia y reparación Mending | Orbes, nivel y equipo pueden cambiar por separado; no interpretar barra fija como prueba de XP perdida. |
| end_mecanica_compartida-020 | Reglas de combate Bedrock | No imponer cooldown, inmunidades, mano secundaria ni IA de Java sobre resultados nativos. |
| end_mecanica_compartida-021 | Gamerules, dificultad y multijugador | Servidor controla mob spawning/griefing, explosiones, keep inventory y recompensas. |
| end_mecanica_compartida-022 | Ítems, bloques o criaturas traídos desde fuera | Rutas compartidas relevantes incluidas; el catálogo no pretende enumerar todos los objetos importables del juego. |

### end_ciudad_botin (26)

| ID | Elemento | Comprobar |
|---|---|---|
| end_ciudad_botin-001 | minecraft:diamond | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-002 | minecraft:iron_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-003 | minecraft:gold_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-004 | minecraft:emerald | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-005 | minecraft:beetroot_seeds | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-006 | minecraft:saddle | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-007 | minecraft:horsearmoriron | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-008 | minecraft:copper_horse_armor | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-009 | minecraft:horsearmorgold | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-010 | minecraft:horsearmordiamond | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-011 | minecraft:diamond_sword (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-012 | minecraft:diamond_spear (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-013 | minecraft:diamond_boots (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-014 | minecraft:diamond_chestplate (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-015 | minecraft:diamond_leggings (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-016 | minecraft:diamond_helmet (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-017 | minecraft:diamond_pickaxe (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-018 | minecraft:diamond_shovel (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-019 | minecraft:iron_sword (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-020 | minecraft:iron_boots (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-021 | minecraft:iron_chestplate (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-022 | minecraft:iron_leggings (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-023 | minecraft:iron_helmet (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-024 | minecraft:iron_pickaxe (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-025 | minecraft:iron_shovel (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| end_ciudad_botin-026 | minecraft:spire_armor_trim_smithing_template | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |

### stronghold_botin_pasillo (23)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_botin_pasillo-001 | minecraft:ender_pearl | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-002 | minecraft:emerald | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-003 | minecraft:diamond | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-004 | minecraft:iron_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-005 | minecraft:gold_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-006 | minecraft:redstone | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-007 | minecraft:bread | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-008 | minecraft:apple | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-009 | minecraft:iron_pickaxe | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-010 | minecraft:iron_sword | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-011 | minecraft:iron_chestplate | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-012 | minecraft:iron_helmet | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-013 | minecraft:iron_leggings | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-014 | minecraft:iron_boots | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-015 | minecraft:golden_apple | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-016 | minecraft:leather | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-017 | minecraft:horsearmoriron | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-018 | minecraft:copper_horse_armor | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-019 | minecraft:horsearmorgold | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-020 | minecraft:horsearmordiamond | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-021 | minecraft:record_otherside | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-022 | minecraft:book (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_pasillo-023 | minecraft:eye_armor_trim_smithing_template | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |

### stronghold_botin_cruce (9)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_botin_cruce-001 | minecraft:iron_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-002 | minecraft:gold_ingot | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-003 | minecraft:redstone | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-004 | minecraft:coal | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-005 | minecraft:bread | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-006 | minecraft:apple | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-007 | minecraft:iron_pickaxe | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-008 | minecraft:book (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_cruce-009 | minecraft:dye (data nativo=0) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |

### stronghold_botin_biblioteca (6)

| ID | Elemento | Comprobar |
|---|---|---|
| stronghold_botin_biblioteca-001 | minecraft:book | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_biblioteca-002 | minecraft:paper | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_biblioteca-003 | minecraft:map | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_biblioteca-004 | minecraft:compass | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_biblioteca-005 | minecraft:book (con encantamientos de tesoro) | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |
| stronghold_botin_biblioteca-006 | minecraft:eye_armor_trim_smithing_template | Mapeo del ítem y variantes; icono/nombre; cantidad; componentes/encantamientos cuando los haya; transferencia y uso de su familia. |

**Total: 246 filas de alcance.** No equivale a 246 pruebas superadas. `catalog.json` y `catalog.csv` permiten añadir resultados por ID sin perder cobertura.
