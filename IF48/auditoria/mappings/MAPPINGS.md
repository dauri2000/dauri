# End y stronghold: verificación de mappings

Revisión de la traducción de datos del addon IF46; estos recursos y las clases de mapping son idénticos en IF47. No se ejecutó Minecraft ni un servidor Bedrock.

Se ejecutó el reescritor real con 89 identificadores y 739 estados nativos en dos modos de IDs (secuencial y hash). Se comprobaron IDs reversibles, nombres/destinos Java, ausencia de fallback y conversión NBT válida. Se revisaron además 17 tipos de entidad, 66 objetos, 40 sonidos, 18 partículas y 7 efectos. Las 6058 comprobaciones son aserciones dentro de una ejecución; incluyen las comprobaciones de serialización de argumentos de partículas.

## Bloques, uno por uno

| Grupo | Identificador Bedrock | Estados | Destinos Java | Resultado |
|---|---|---:|---|---|
| End_natural | `minecraft:air` | 1 | `minecraft:air` | IDs traducidos |
| End_natural | `minecraft:end_stone` | 1 | `minecraft:end_stone` | IDs traducidos |
| End_natural | `minecraft:end_bricks` | 1 | `minecraft:end_stone_bricks` | IDs traducidos |
| End_natural | `minecraft:purpur_block` | 3 | `minecraft:purpur_block` | IDs traducidos |
| End_natural | `minecraft:purpur_pillar` | 3 | `minecraft:purpur_pillar` | IDs traducidos |
| End_natural | `minecraft:purpur_slab` | 2 | `minecraft:purpur_slab` | IDs traducidos |
| End_natural | `minecraft:purpur_double_slab` | 2 | `minecraft:purpur_slab` | IDs traducidos |
| End_natural | `minecraft:purpur_stairs` | 40 | `minecraft:purpur_stairs` | IDs traducidos |
| End_natural | `minecraft:chorus_plant` | 1 | `minecraft:chorus_plant` | IDs traducidos |
| End_natural | `minecraft:chorus_flower` | 6 | `minecraft:chorus_flower` | IDs traducidos |
| End_natural | `minecraft:obsidian` | 1 | `minecraft:obsidian` | IDs traducidos |
| End_natural | `minecraft:bedrock` | 2 | `minecraft:bedrock` | IDs traducidos |
| End_natural | `minecraft:iron_bars` | 16 | `minecraft:iron_bars` | IDs traducidos |
| End_natural | `minecraft:end_rod` | 6 | `minecraft:end_rod` | IDs traducidos |
| End_natural | `minecraft:chest` | 4 | `minecraft:chest` | IDs traducidos |
| End_natural | `minecraft:ender_chest` | 4 | `minecraft:ender_chest` | IDs traducidos |
| End_natural | `minecraft:dragon_egg` | 1 | `minecraft:dragon_egg` | IDs traducidos |
| End_natural | `minecraft:end_gateway` | 1 | `minecraft:end_gateway` | IDs traducidos |
| End_natural | `minecraft:end_portal` | 1 | `minecraft:end_portal` | IDs traducidos |
| End_natural | `minecraft:dragon_head` | 6 | `minecraft:dragon_head`, `minecraft:dragon_wall_head` | IDs traducidos |
| End_natural | `minecraft:magenta_stained_glass` | 1 | `minecraft:magenta_stained_glass` | IDs traducidos |
| End_natural | `minecraft:standing_banner` | 16 | `minecraft:black_banner` | IDs traducidos |
| End_natural | `minecraft:wall_banner` | 6 | `minecraft:black_wall_banner` | IDs traducidos |
| End_natural | `minecraft:brewing_stand` | 8 | `minecraft:brewing_stand` | IDs traducidos |
| End_natural | `minecraft:ladder` | 6 | `minecraft:ladder` | IDs traducidos |
| End_natural | `minecraft:torch` | 6 | `minecraft:torch`, `minecraft:wall_torch` | IDs traducidos |
| End_natural | `minecraft:fire` | 16 | `minecraft:fire` | IDs traducidos |
| End_natural | `minecraft:frame` | 24 | `minecraft:air` | AIR previsto; se representa mediante entidad Java |
| Stronghold | `minecraft:stone` | 1 | `minecraft:stone` | IDs traducidos |
| Stronghold | `minecraft:cobblestone` | 1 | `minecraft:cobblestone` | IDs traducidos |
| Stronghold | `minecraft:mossy_cobblestone` | 1 | `minecraft:mossy_cobblestone` | IDs traducidos |
| Stronghold | `minecraft:stone_bricks` | 1 | `minecraft:stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:mossy_stone_bricks` | 1 | `minecraft:mossy_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:cracked_stone_bricks` | 1 | `minecraft:cracked_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:chiseled_stone_bricks` | 1 | `minecraft:chiseled_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:infested_stone` | 1 | `minecraft:infested_stone` | IDs traducidos |
| Stronghold | `minecraft:infested_cobblestone` | 1 | `minecraft:infested_cobblestone` | IDs traducidos |
| Stronghold | `minecraft:infested_stone_bricks` | 1 | `minecraft:infested_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:infested_mossy_stone_bricks` | 1 | `minecraft:infested_mossy_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:infested_cracked_stone_bricks` | 1 | `minecraft:infested_cracked_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:infested_chiseled_stone_bricks` | 1 | `minecraft:infested_chiseled_stone_bricks` | IDs traducidos |
| Stronghold | `minecraft:stone_brick_slab` | 2 | `minecraft:stone_brick_slab` | IDs traducidos |
| Stronghold | `minecraft:stone_brick_double_slab` | 2 | `minecraft:stone_brick_slab` | IDs traducidos |
| Stronghold | `minecraft:stone_brick_stairs` | 40 | `minecraft:stone_brick_stairs` | IDs traducidos |
| Stronghold | `minecraft:smooth_stone_slab` | 2 | `minecraft:smooth_stone_slab` | IDs traducidos |
| Stronghold | `minecraft:smooth_stone_double_slab` | 2 | `minecraft:smooth_stone_slab` | IDs traducidos |
| Stronghold | `minecraft:oak_slab` | 2 | `minecraft:oak_slab` | IDs traducidos |
| Stronghold | `minecraft:oak_double_slab` | 2 | `minecraft:oak_slab` | IDs traducidos |
| Stronghold | `minecraft:oak_stairs` | 40 | `minecraft:oak_stairs` | IDs traducidos |
| Stronghold | `minecraft:oak_planks` | 1 | `minecraft:oak_planks` | IDs traducidos |
| Stronghold | `minecraft:oak_fence` | 16 | `minecraft:oak_fence` | IDs traducidos |
| Stronghold | `minecraft:wooden_door` | 32 | `minecraft:oak_door` | IDs traducidos |
| Stronghold | `minecraft:iron_door` | 32 | `minecraft:iron_door` | IDs traducidos |
| Stronghold | `minecraft:stone_button` | 12 | `minecraft:stone_button` | IDs traducidos |
| Stronghold | `minecraft:stone_pressure_plate` | 16 | `minecraft:stone_pressure_plate` | IDs traducidos |
| Stronghold | `minecraft:wooden_pressure_plate` | 16 | `minecraft:oak_pressure_plate` | IDs traducidos |
| Stronghold | `minecraft:bookshelf` | 1 | `minecraft:bookshelf` | IDs traducidos |
| Stronghold | `minecraft:web` | 1 | `minecraft:cobweb` | IDs traducidos |
| Stronghold | `minecraft:mob_spawner` | 1 | `minecraft:spawner` | IDs traducidos |
| Stronghold | `minecraft:water` | 16 | `minecraft:water` | IDs traducidos |
| Stronghold | `minecraft:flowing_water` | 16 | `minecraft:water` | IDs traducidos |
| Stronghold | `minecraft:lava` | 16 | `minecraft:lava` | IDs traducidos |
| Stronghold | `minecraft:flowing_lava` | 16 | `minecraft:lava` | IDs traducidos |
| Stronghold | `minecraft:end_portal_frame` | 8 | `minecraft:end_portal_frame` | IDs traducidos |
| End_related_carried | `minecraft:bed` | 16 | `minecraft:white_bed` | IDs traducidos |
| End_related_carried | `minecraft:respawn_anchor` | 5 | `minecraft:respawn_anchor` | IDs traducidos |
| End_related_carried | `minecraft:undyed_shulker_box` | 1 | `minecraft:shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:white_shulker_box` | 1 | `minecraft:white_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:orange_shulker_box` | 1 | `minecraft:orange_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:magenta_shulker_box` | 1 | `minecraft:magenta_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:light_blue_shulker_box` | 1 | `minecraft:light_blue_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:yellow_shulker_box` | 1 | `minecraft:yellow_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:lime_shulker_box` | 1 | `minecraft:lime_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:pink_shulker_box` | 1 | `minecraft:pink_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:gray_shulker_box` | 1 | `minecraft:gray_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:light_gray_shulker_box` | 1 | `minecraft:light_gray_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:cyan_shulker_box` | 1 | `minecraft:cyan_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:purple_shulker_box` | 1 | `minecraft:purple_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:blue_shulker_box` | 1 | `minecraft:blue_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:brown_shulker_box` | 1 | `minecraft:brown_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:green_shulker_box` | 1 | `minecraft:green_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:red_shulker_box` | 1 | `minecraft:red_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:black_shulker_box` | 1 | `minecraft:black_shulker_box` | IDs traducidos |
| End_related_carried | `minecraft:anvil` | 4 | `minecraft:anvil` | IDs traducidos |
| End_related_carried | `minecraft:crafting_table` | 1 | `minecraft:crafting_table` | IDs traducidos |
| End_related_carried | `minecraft:end_brick_stairs` | 40 | `minecraft:end_stone_brick_stairs` | IDs traducidos |
| End_related_carried | `minecraft:end_stone_brick_slab` | 2 | `minecraft:end_stone_brick_slab` | IDs traducidos |
| End_related_carried | `minecraft:end_stone_brick_double_slab` | 2 | `minecraft:end_stone_brick_slab` | IDs traducidos |
| End_related_carried | `minecraft:end_stone_brick_wall` | 162 | `minecraft:end_stone_brick_wall` | IDs traducidos |

La conexión visual/física entre bloques y la generación de estructuras no quedan comprobadas por estos mappings. `chorus_plant` conserva una sola entrada nativa con conexiones Java iniciales; la corrección de conexiones se estudia aparte. Los marcos se traducen a aire como bloque porque `ChunkTracker` y `EntityTracker` crean entidades de marco.

## Entidades

| Bedrock | Java | Campos de metadata disponibles |
|---|---|---:|
| `minecraft:ender_dragon` | `minecraft:ender_dragon` | 17 |
| `minecraft:ender_crystal` | `minecraft:end_crystal` | 10 |
| `minecraft:enderman` | `minecraft:enderman` | 19 |
| `minecraft:endermite` | `minecraft:endermite` | 16 |
| `minecraft:shulker` | `minecraft:shulker` | 19 |
| `minecraft:shulker_bullet` | `minecraft:shulker_bullet` | 8 |
| `minecraft:dragon_fireball` | `minecraft:dragon_fireball` | 8 |
| `minecraft:area_effect_cloud` | `minecraft:area_effect_cloud` | 11 |
| `minecraft:ender_pearl` | `minecraft:ender_pearl` | 9 |
| `minecraft:eye_of_ender_signal` | `minecraft:eye_of_ender` | 9 |
| `minecraft:xp_orb` | `minecraft:experience_orb` | 9 |
| `minecraft:item` | `minecraft:item` | 9 |
| `minecraft:falling_block` | `minecraft:falling_block` | 9 |
| `minecraft:silverfish` | `minecraft:silverfish` | 16 |
| `minecraft:arrow` | `minecraft:arrow` | 12 |
| `minecraft:thrown_trident` | `minecraft:trident` | 13 |
| `minecraft:fireworks_rocket` | `minecraft:firework_rocket` | 11 |

Estos resultados comprueban el tipo y el esquema de metadata declarado, no la IA, el daño, las partes del dragón, la posición o el movimiento.

## Objetos

| Bedrock | Variantes de mapping | Destinos Java |
|---|---:|---|
| `minecraft:end_stone` | 1 | `minecraft:end_stone` |
| `minecraft:end_bricks` | 1 | `minecraft:end_stone_bricks` |
| `minecraft:purpur_block` | 1 | `minecraft:purpur_block` |
| `minecraft:purpur_pillar` | 3 | `minecraft:purpur_pillar` |
| `minecraft:purpur_slab` | 1 | `minecraft:purpur_slab` |
| `minecraft:purpur_stairs` | 40 | `minecraft:purpur_stairs` |
| `minecraft:chorus_flower` | 6 | `minecraft:chorus_flower` |
| `minecraft:chorus_fruit` | 1 | `minecraft:chorus_fruit` |
| `minecraft:popped_chorus_fruit` | 1 | `minecraft:popped_chorus_fruit` |
| `minecraft:dragon_egg` | 1 | `minecraft:dragon_egg` |
| `minecraft:dragon_head` | 6 | `minecraft:dragon_head` |
| `minecraft:end_rod` | 6 | `minecraft:end_rod` |
| `minecraft:ender_pearl` | 1 | `minecraft:ender_pearl` |
| `minecraft:ender_eye` | 1 | `minecraft:ender_eye` |
| `minecraft:end_crystal` | 1 | `minecraft:end_crystal` |
| `minecraft:elytra` | 1 | `minecraft:elytra` |
| `minecraft:firework_rocket` | 1 | `minecraft:firework_rocket` |
| `minecraft:shulker_shell` | 1 | `minecraft:shulker_shell` |
| `minecraft:undyed_shulker_box` | 1 | `minecraft:shulker_box` |
| `minecraft:dragon_breath` | 1 | `minecraft:dragon_breath` |
| `minecraft:glass_bottle` | 1 | `minecraft:glass_bottle` |
| `minecraft:lingering_potion` | 48 | `minecraft:lingering_potion` |
| `minecraft:potion` | 48 | `minecraft:potion` |
| `minecraft:splash_potion` | 48 | `minecraft:splash_potion` |
| `minecraft:bow` | 1 | `minecraft:bow` |
| `minecraft:crossbow` | 1 | `minecraft:crossbow` |
| `minecraft:arrow` | 49 | `minecraft:arrow`, `minecraft:tipped_arrow` |
| `minecraft:shield` | 1 | `minecraft:shield` |
| `minecraft:carved_pumpkin` | 4 | `minecraft:carved_pumpkin` |
| `minecraft:pumpkin` | 4 | `minecraft:pumpkin` |
| `minecraft:diamond_sword` | 1 | `minecraft:diamond_sword` |
| `minecraft:diamond_pickaxe` | 1 | `minecraft:diamond_pickaxe` |
| `minecraft:diamond_shovel` | 1 | `minecraft:diamond_shovel` |
| `minecraft:diamond_boots` | 1 | `minecraft:diamond_boots` |
| `minecraft:diamond_chestplate` | 1 | `minecraft:diamond_chestplate` |
| `minecraft:diamond_helmet` | 1 | `minecraft:diamond_helmet` |
| `minecraft:diamond_leggings` | 1 | `minecraft:diamond_leggings` |
| `minecraft:iron_sword` | 1 | `minecraft:iron_sword` |
| `minecraft:iron_pickaxe` | 1 | `minecraft:iron_pickaxe` |
| `minecraft:iron_shovel` | 1 | `minecraft:iron_shovel` |
| `minecraft:iron_boots` | 1 | `minecraft:iron_boots` |
| `minecraft:iron_chestplate` | 1 | `minecraft:iron_chestplate` |
| `minecraft:iron_helmet` | 1 | `minecraft:iron_helmet` |
| `minecraft:iron_leggings` | 1 | `minecraft:iron_leggings` |
| `minecraft:diamond` | 1 | `minecraft:diamond` |
| `minecraft:emerald` | 1 | `minecraft:emerald` |
| `minecraft:iron_ingot` | 1 | `minecraft:iron_ingot` |
| `minecraft:gold_ingot` | 1 | `minecraft:gold_ingot` |
| `minecraft:saddle` | 1 | `minecraft:saddle` |
| `minecraft:iron_horse_armor` | 1 | `minecraft:iron_horse_armor` |
| `minecraft:golden_horse_armor` | 1 | `minecraft:golden_horse_armor` |
| `minecraft:diamond_horse_armor` | 1 | `minecraft:diamond_horse_armor` |
| `minecraft:beetroot_seeds` | 1 | `minecraft:beetroot_seeds` |
| `minecraft:enchanted_book` | 1 | `minecraft:enchanted_book` |
| `minecraft:book` | 1 | `minecraft:book` |
| `minecraft:paper` | 1 | `minecraft:paper` |
| `minecraft:empty_map` | 4 | `minecraft:map` |
| `minecraft:filled_map` | 15 | `minecraft:filled_map` |
| `minecraft:compass` | 1 | `minecraft:compass` |
| `minecraft:water_bucket` | 1 | `minecraft:water_bucket` |
| `minecraft:bucket` | 1 | `minecraft:bucket` |
| `minecraft:golden_apple` | 1 | `minecraft:golden_apple` |
| `minecraft:experience_bottle` | 1 | `minecraft:experience_bottle` |
| `minecraft:totem_of_undying` | 1 | `minecraft:totem_of_undying` |
| `minecraft:blaze_powder` | 1 | `minecraft:blaze_powder` |
| `minecraft:nether_star` | 1 | `minecraft:nether_star` |

Se comprobó la existencia de cada destino y su ID. No equivale a verificar todos sus usos, contenidos, encantamientos o renderizado.

## Sonidos

| Bedrock | Java |
|---|---|
| `armor.equip_elytra` | `minecraft:item.armor.equip_elytra` |
| `block.chorusflower.death` | `minecraft:block.chorus_flower.death` |
| `block.chorusflower.grow` | `minecraft:block.chorus_flower.grow` |
| `block.end_portal.spawn` | `minecraft:block.end_portal.spawn` |
| `block.end_portal_frame.fill` | `minecraft:block.end_portal_frame.fill` |
| `bottle.dragonbreath` | `minecraft:item.bottle.fill_dragonbreath` |
| `elytra.loop` | `minecraft:item.elytra.flying` |
| `mob.enderdragon.death` | `minecraft:entity.ender_dragon.death` |
| `mob.enderdragon.flap` | `minecraft:entity.ender_dragon.flap` |
| `mob.enderdragon.growl` | `minecraft:entity.ender_dragon.growl` |
| `mob.enderdragon.hit` | `minecraft:entity.ender_dragon.hurt` |
| `mob.endermen.death` | `minecraft:entity.enderman.death` |
| `mob.endermen.hit` | `minecraft:entity.enderman.hurt` |
| `mob.endermen.idle` | `minecraft:entity.enderman.ambient` |
| `mob.endermen.portal` | `minecraft:entity.enderman.teleport` |
| `mob.endermen.scream` | `minecraft:entity.enderman.scream` |
| `mob.endermen.stare` | `minecraft:entity.enderman.stare` |
| `mob.endermite.hit` | `minecraft:entity.endermite.hurt` |
| `mob.endermite.kill` | `minecraft:entity.endermite.death` |
| `mob.endermite.say` | `minecraft:entity.endermite.ambient` |
| `mob.endermite.step` | `minecraft:entity.endermite.step` |
| `mob.imitate.enderdragon` | `minecraft:entity.parrot.imitate.ender_dragon` |
| `mob.imitate.endermen` | `minecraft:entity.enderman.ambient` |
| `mob.imitate.endermite` | `minecraft:entity.parrot.imitate.endermite` |
| `mob.imitate.shulker` | `minecraft:entity.parrot.imitate.shulker` |
| `mob.shulker.ambient` | `minecraft:entity.shulker.ambient` |
| `mob.shulker.bullet.hit` | `minecraft:entity.shulker_bullet.hit` |
| `mob.shulker.close` | `minecraft:entity.shulker.close` |
| `mob.shulker.close.hurt` | `minecraft:entity.shulker.hurt_closed` |
| `mob.shulker.death` | `minecraft:entity.shulker.death` |
| `mob.shulker.hurt` | `minecraft:entity.shulker.hurt` |
| `mob.shulker.open` | `minecraft:entity.shulker.open` |
| `mob.shulker.shoot` | `minecraft:entity.shulker.shoot` |
| `mob.shulker.teleport` | `minecraft:entity.shulker.teleport` |
| `music.game.endboss` | `minecraft:music.dragon` |
| `note.enderdragon` | `minecraft:block.note_block.imitate.ender_dragon` |
| `random.enderchestclosed` | `minecraft:block.ender_chest.close` |
| `random.enderchestopen` | `minecraft:block.ender_chest.open` |
| `random.shulkerboxclosed` | `minecraft:block.shulker_box.close` |
| `random.shulkerboxopen` | `minecraft:block.shulker_box.open` |

## Partículas

| Bedrock | Java | Argumentos |
|---|---|
| `minecraft:basic_portal_particle` | `minecraft:portal` |  |
| `minecraft:dragon_breath_fire` | `minecraft:dragon_breath` | FloatType=1.0 |
| `minecraft:dragon_breath_lingering` | `minecraft:dragon_breath` | FloatType=1.0 |
| `minecraft:dragon_breath_trail` | `minecraft:dragon_breath` | FloatType=1.0 |
| `minecraft:dragon_death_explosion_emitter` | `minecraft:explosion_emitter` |  |
| `minecraft:dragon_destroy_block` | `minecraft:explosion` |  |
| `minecraft:dragon_dying_explosion` | `minecraft:explosion` |  |
| `minecraft:end_chest` | `minecraft:portal` |  |
| `minecraft:endrod` | `minecraft:end_rod` |  |
| `minecraft:eye_of_ender_bubble_particle` | `minecraft:bubble` |  |
| `minecraft:eyeofender_death_explode_particle` | `minecraft:dust` | IntType=-45851, FloatType=2.5 |
| `minecraft:falling_dust_dragon_egg_particle` | `minecraft:falling_dust` | VarIntType=11212 |
| `minecraft:mob_portal` | `minecraft:portal` |  |
| `minecraft:portal_directional` | `minecraft:portal` |  |
| `minecraft:portal_east_west` | `minecraft:portal` |  |
| `minecraft:portal_north_south` | `minecraft:portal` |  |
| `minecraft:portal_reverse_particle` | `minecraft:reverse_portal` |  |
| `minecraft:shulker_bullet` | `minecraft:end_rod` |  |

Los seis argumentos de partículas con payload se escribieron y leyeron usando sus tipos reales. Para aliento del dragón se comprobó FLOAT=1; para el polvo del ojo de ender, INT+FLOAT. No se renderizaron partículas.

## Efectos

| Bedrock | Java |
|---|---|
| `levitation` | `minecraft:levitation` |
| `slow_falling` | `minecraft:slow_falling` |
| `instant_damage` | `minecraft:instant_damage` |
| `regeneration` | `minecraft:regeneration` |
| `resistance` | `minecraft:resistance` |
| `jump_boost` | `minecraft:jump_boost` |
| `invisibility` | `minecraft:invisibility` |

## Fronteras de la ejecución

- Via platform and manager log/protocol lookup proxies; unsupported operations throw
- One ArgumentTypeRegistry.init call omitted in test-only loader bytecode because Brigadier dependency unavailable; production mod untouched
- StructuredDataContainer lookup names come from actual bundled java/via_mappings.json with identity IDs for empty item-particle payloads; live Java protocol startup not executed
- ResourcePackStorage empty, loadedOnJavaClient=false: vanilla mappings only, server custom packs not tested

No hubo cambios en el mod. `verification.json` conserva cada estado y cada ID observado; `manifest.json` conserva hashes y procedencia.
