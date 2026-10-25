# Controlled End/stronghold mapping checks

`EndMappingProbe.java` executes the bundled IF46 `BedrockMappingData.load` and actual `BlockStateRewriter`, not a Python reproduction of their logic. The IF46 core used by the test was compared byte-for-byte with the core inside the delivered IF46 JAR. All 51 mapping data resources and four tested mapping/model classes are identical inside IF47. The manifest records hashes.

## Result

- 89 current native block identifiers, all 739 palette states, checked in both sequential and hashed runtime-ID modes. Every tested state has reversible native IDs, an existing declared Java destination, no missing-mapping fallback, and stable conversion through the actual NBT upgrader/sanitizer.
- 17 entity types and metadata layouts, 66 item identifiers and every variant in their mapping tables, 40 selected sounds, 18 selected particles, and seven effects.
- All six particle arguments were serialized and decoded with their actual ViaVersion types. Dragon-breath arguments are FLOAT=1; eye-of-ender dust is INT+FLOAT.
- 6058 assertions in one controlled execution. These are not 6058 independent tests or 6058 game mechanics.

`MAPPINGS.md` lists every identifier, one row at a time. `verification.json` additionally records every block state and native/Java ID. `minecraft:frame` intentionally maps to an air block: actual `ChunkTracker` and `EntityTracker` represent it as a Java item-frame entity. The crystal is an entity (`minecraft:ender_crystal`) and item (`minecraft:end_crystal`), not a native block. The empty map's current native name is `minecraft:empty_map`.

## Explicit execution boundaries

The command-registry bootstrap requires Brigadier, absent in this environment. Both dependency-host requests returned 403. `CommandBootstrapBoundary.java` removes exactly one `INVOKESTATIC ArgumentTypeRegistry.init()V` from a test-only copy of the mapping loader. It does not edit the production JAR, mappings, or rewriter. All other mapping-loader instructions remain. Command parsing is outside this check.

Via platform/manager/protocol lookup proxies supply the logger and data-component serializer names from the actual bundled Java mapping resource. The serializer lookup is identity mapping for the empty item particle payloads parsed during startup; no live Java protocol pipeline is initialized. Unsupported boundary calls throw. The real `ResourcePackStorage` is empty and reports that custom packs are not loaded, so the check covers vanilla mapping data only.

No Minecraft renderer, Fabric/Mixin transformation, game physics, server world generation, gameplay or Bedrock server was executed. Valid translation IDs do not establish correct collision, interaction, AI, damage or live behavior. Chorus-plant geometry is reviewed separately.

## Reproduction

Use JDK 25 and the classpath recorded in `classpath.txt`. Compile both Java sources with the local ASM dependency added for `CommandBootstrapBoundary`, run that helper on `if46-core.jar` into a test-only `boundary` directory, then run `EndMappingProbe` with `boundary` first and `-Xverify:all`. Never copy the boundary class into a delivered mod.

No production files, user worlds, videos or logs were changed or published by this audit.
