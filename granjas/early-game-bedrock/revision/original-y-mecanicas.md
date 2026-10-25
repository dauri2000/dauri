# Independent Bedrock adaptation review

The uploaded original was independently decoded from gzip/NBT and its packed6-bit palette. All155925 block indices and all34 palette entries match the root agent's decode. The original remains unchanged: SHA256 `d2ca5fbdc4e4196d5977fb1db7098a8f88f79c903c4f22d605dae19a7ebfff37`,4448bytes; LitematicVersion6 / MinecraftDataVersion3120. Negative region size normalizes to bounds `(0,0,0)`–`(44,76,44)`. Details are in `independent-nbt-summary.json`. Coordinates below use those normalized schematic bounds, not the player's world origin.

## Concrete original design and corrections

- There are13 diamond platforms, `F=34+3k` for`k=0..12`, centered at`(22,F,22)`. Each has112 top slabs and one upward-facing dispenser. Their surface at`F+1` and the3-block spacing can be preserved.
- All13 dispensers contain an **empty bucket**, while sources already exist at`(22,F+1,22)`. This is a captured wet/paused phase. A dry-start adaptation must remove only these13 flushing sources and their platform flows, and provide filled buckets; permanent collector water must remain. There are64 permanent sources atY34 around the collector's outer ring, distinct from the thirteen center sources.
- Observers at`(22,F+2,22)` face up and output down into the water/air cell above the lower dispenser. They do not directly touch that dispenser. This is the Java quasi-connectivity dependency that needs a Bedrock direct-power design. Keep`(22,F+1,22)` free for the upward dispenser's water. A local direct-power module could use an east-facing observer at`(23,F,22)` to output west into the dispenser and watch a bus at`(24,F,22)`; that is only a module constraint, not a tested full clock design.
- One of the25 collector gates is closed at`(21,32,20)`; opening it removes an actual physical obstacle. The other24 are already open. The gates occupy a5×5 chute over the25 lower collection hoppers.
- The lower hoppers converge on`(24,0,22)`, facing east into air. A receiving chest at`(25,0,22)` makes the existing collection path usable without redesigning the hopper grid. Clear captured loot from collection inventories in a fresh-build file; retain deliberate clock/bucket contents or explain manual loading.
- The clock snapshot atY74–75 includes a locked hopper, powered lever,16 observer items and one extended sticky piston/head. Those dynamic states are not an independent guarantee of a running Bedrock clock. A Bedrock clock should be initialized deliberately. Any16 ordinary64-stackable items can provide the same hopper-transfer count; observers are not required as clock fuel.

## What is and is not proven about edition differences

Pinned PowerNukkitX source corroborates adjacent redstone activation and observer output opposite its observed face; an observer two cells above a dispenser with air/water between is not a direct connection. The final bus/clock still requires explicit validation; no BDS process or redstone simulation ran here.

Waterlogging does not imply that flowing water passes through a gate. PNX FenceGate uses waterloggingLevel1, while `Block.canWaterloggingFlowInto` requires canBeFlowedInto or level>1 and its liquid flow logic rejects the level1 gate. Thus replacing every gate merely because Bedrock supports source waterlogging would be unjustified. This is third-party source corroboration, not vanilla BDS execution. Exact files, URLs and hashes are in `native-water/manifest.json`.

There is no demonstrated vanilla Bedrock rule in the obtained evidence that forbids mob spawning on these **upper** slabs. PNX explicitly exposes a solid top for an upper slab, but its spawn check is too permissive to stand in for BDS. If reliability takes priority over material identity, replacing each spawning top slab with a full block at the same coordinate preserves the top surface and headroom; describe that as conservative normalization, not a proven mandatory fix.

Roof opacity remains unresolved; see `roof/findings.md`. Full opaque roof blocks plus upper bottom slabs are a conservative option, not a claim that the original bottom-slab roof fails. Official sample brightness filters alone do not capture every engine spawn condition, so they must not be used to promise a rate or validate the chamber's actual light.

The farm flushes mobs physically; it contains no trapdoor pathfinding lure. Official entity data shows differing water navigation and spider climbing, so no assumption that every mob voluntarily walks into water is needed or justified. Spider accumulation/climbing is a possible practical limit, not a proven failure of this exact build.

## Simulation distance, AFK and drops

The user confirmed simulation distance6+. An AFK feet position around`(22.5,100,22.5)` puts platform spawn surfaces roughly29–65.4blocks away and the hopper landing about99blocks away, comfortably inside the intended24–128 operating envelope. This preserves the13 floors and77-block structure height; it is not a measured spawn-rate guarantee. A3×3 glass platform atY99 with coverY102 leaves standing space atY100–101. Access can be explained as temporary scaffolding/pillaring rather than altering the farm with a permanent ladder.

The open chute atY32 to the hopper top aroundY1 gives roughly31blocks of fall. That kills ordinary20-health monsters, so this is primarily a drop farm. Official sample experience rewards depend on player credit; automatic fall kills must not be advertised as automatic XP collection. Spider health/width/climbing and unusual armored/equipped mobs can affect practical results.

## Evidence and limits

`spawning/findings.md` contains pinned Mojang bedrock-samples, Microsoft documentation and PNX references, with explicit source-versus-engine limits. `roof/` records the unsuccessful public roof-source probes. `native-water/` contains fixed PNX source snapshots. No original/final .litematic was edited by this audit, no user media was published and no actual Minecraft/Bedrock-server behavior was claimed tested.

Preserve the original DataVersion3120 when keeping legacy item NBT `{Count,id}`; raising the version without migrating its contents can bypass the intended loader data-fix path. The final file writer owns version compatibility and structural verification.
