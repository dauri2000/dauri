# Independent review of the frozen chorus-plant fix

The review passed on the two candidate hashes recorded in `verification.json`. No candidate class was rebuilt or edited.

## Eight check groups

1. An independent ASM reader extracts the original `ConnectedBlockShapes` from the delivered IF46 core, verifies the two new guards and their exact argument positions, removes only those guards in memory, and obtains canonical equality of the entire target class with IF46. All previous methods and their instructions are preserved.
2. An independent ASM `BasicVerifier` checks every method of both frozen candidate classes.
3. A stale six-arm input resets from unknown neighbors; its original properties remain unchanged. The generic solid-block predicate is never invoked for chorus.
4. Actual `ChunkTracker` lookups handle asymmetric connections across negative-coordinate chunk boundaries. The native palette is unchanged by Java projection.
5. Actual minimum/maximum End-height lookups leave out-of-world neighbors disconnected.
6. The actual border-refresh path sends the correct updated disconnection to a remaining negative-coordinate chunk after its neighboring received palette is removed.
7. A diagonal flower creates no axis arm.
8. A separate baseline/candidate process comparison gives identical output for 120 nonchorus cases: ten block types × six neighbor fields × two solid-predicate results. It includes fences, stairs, purpur stairs, kelp, redstone, flowers, air, stone and custom namespace plants.

The owner's 13 checks were reviewed, including all 64 arm combinations against actual registered Java blockstates, six flower directions, the DOWN-only end-stone rule, initial chunk remapping, neighbor changes, newly loaded borders and vertical section boundaries. Those 13 are **not** counted again in this independent report.

## Bounds and interpretation

`IndependentChorusProbe` uses the owner's fixture only for minimal Java registry bootstrap, supplied received palettes, session and transport. The frozen production shape/helper and core `ChunkTracker` execute directly. The border-removal case invokes the existing refresh method after removing the neighboring received palette; it does not execute every step of the complete `unloadChunk` handler.

The helper reconstructs canonical Java arms: chorus stems and flowers in six directions, plus end stone below. This fixes the missing arms in Java's mapped chorus blockstate. It does not establish exact live Bedrock collision behavior for artificially placed lateral end-stone neighbors; third-party Bedrock implementations differ here. No Minecraft renderer, Mixin engine, Fabric gameplay or actual Bedrock server was executed.

Nonchorus equality demonstrates preservation of the sampled historical behavior, not that all previous fence/stair/kelp/redstone behavior is bug-free. No packets to a server, inventory writes, native block writes, world generation, drops or growth rules were added by the helper.
