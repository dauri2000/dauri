# Independent fireworks review — final frozen candidate

The independent review passes **15 groups**:12 actual handler/wire/scope groups and3 bytecode isolation groups. Nine actual binary request encodings are inspected inside those groups and are not added again. The same corrected-behavior endpoint test fails with the unmodified IF48 matcher because the output click has no matching recipe.

The reviewer did not rebuild or edit production candidates or run the owner's generator. Final hashes are in `pinned-class-hashes.json` and `verification.json`.

## What executes

`IndependentFireworkWireProbe.java` uses the old stable display/session/transport fixture, with its display-only Java item mapping explicitly retained. It supplies a new independent sparse grid, only an announced native MULTI fireworks family, and native item IDs loaded from the shipped runtime resource. It executes the actual candidate matcher and unchanged IF48 table/2×2 output handlers, request prediction, `PacketFactory`, request response processing, rollback, bulk continuation and tagged-item synchronization gate.

The actual `ArrayType<ItemStackRequestInfo>` / `ItemStackRequestType` / item result serializers then write bytes. A separate parser inspects the whole binary payload, including current and compatibility action discriminators, namespaced result descriptor, little-endian native NBT, all slot/container/network IDs, filter strings/origin and absence of trailing bytes. This is an inspection of bytes emitted by the shipped writer, **not validation by a native server decoder**.

The tests establish:

- Current announced recipe ID64009 reaches `CraftRecipe`; replacing the announcement with75017 cancels old bulk continuation and changes future matches. No fixture4758 ID or fabricated ID is substituted.
- Three rockets are described per craft. Flight2/3 and complete native star explosion data survive binary serialization, including an extra native field.
- Every occupied native ingredient slot receives one `Consume` entry with the exact per-batch count and current stack network ID. Stack quantity26 is not interpreted as26 ingredient slots.
- `Place` references CreatedOutput container60, slot50 and the current request ID; destinations are cursor59 or combined inventory12. There is no additional explicit `Create` action in this unchanged output pipeline.
- Table and2×2 one-click bulk paths emit21+5 serialized crafts and78 rockets. Success ACK alone does not continue. Three of four full authoritative inventory groups also do not continue. The fourth completes the existing NBT gate and resumes exactly once using acknowledged stack IDs.
- Native rejection rolls back full quantities, NBT and IDs and stops continuation.
- Missing/wrong family, network ID0 and wrong station do not generate output requests. Queries preserve registered recipes and input stacks, and their ephemeral NBT copies cannot modify the native star or another result.
- Original custom rocket recipes outside vanilla cardinality and explicit original firework-star results preserve their exact original data rather than being replaced.
- A star missing mandatory validated Fade data fails closed without consumption. This is a supported-format boundary, **not evidence that every legitimate native star always has that field**.

## Bytecode isolation

`IndependentFireworkBytecodeCheck.java` removes precisely the seven independently identified added instructions before the sole matcher ARETURN. The complete target then matches original IF48 canonical bytecode. Both frozen classes pass all-method ASM BasicVerifier. The helper has no packet/request/bulk/session/container mutator references. Original container, bulk, synchronization, ACK and packet serializer code executes from the IF48 core.

## Limits and reproduction

The supplied backend response model independently applies decoded request bytes, then provides controlled acknowledgments and full inventory contents. It does not emulate native crafting acceptance, anti-cheat, plugins, network timing or actual BDS. No Minecraft renderer, Mixin engine, GUI or live server executes.

Compile both independent `.java` files with Java25 and the recorded `classpath.txt`; the bytecode checker also needs the ASM classpath. Put `independent/test-classes` first, then the frozen `candidate`, then `if48-core.jar`, then the dependencies/old stable fixture in the recorded order. Run the wire probe under `-Xverify:all`. For the baseline remove only the candidate directory from the runtime classpath. The bytecode checker receives IF48 core JAR, candidate CraftingDataTracker class and candidate FireworkMultiRecipes class. Output and exact hashes are recorded locally; no private user log is involved.
