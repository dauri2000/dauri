# Redstone and central-column audit

This audit reads `original-decoded.json` only. It does not modify the original schematic, build a final schematic, execute Minecraft, or simulate BDS redstone/fluid behavior. Coordinates are schematic-local `(x,y,z)`; array index `(y*45+z)*45+x`.

## Original geometry

There are 13 identical central floors, `F = 34,37,40,...,70`. Each has:

- Upward dispenser `(22,F,22)` holding one empty bucket.
- Water source `(22,F+1,22)`; the saved schematic is in its flooded phase.
- Upward-facing observer `(22,F+2,22)`, with its output down into the source cell. The dispenser two blocks below needs Java quasi-connectivity for that output.

There is a 14th upward observer at `(22,73,22)`, below comparator `(22,74,22)`. The Etho-style hopper clock is above the roof. Its two mutually facing hoppers are at `(22,74,20)` and `(22,74,21)`, containing 16 stackable items in total. The original lever `(23,74,23)` is powered and attached to solid `(22,74,23)`.

Only gate `(21,32,20)` is closed; the other 24 gates at y32 are open. The complete 5×5 shaft `(20..24,1..31,20..24)` is empty. The hopper array is y0, not just below y32: there are 31 empty block layers between gate layer and hopper layer. This is geometry, not a guarantee of death for every equipment/status variant.

## Recommended direct column

For every F, use a dry column at x23,z22:

| Position | Block/function |
|---|---|
| `(23,F+2,22)` | observer facing UP: looks at dust above; output DOWN directly into dispenser |
| `(23,F+1,22)` | dispenser facing WEST: puts/collects water in original source `(22,F+1,22)` |
| `(23,F,22)` | dust: receives strong power through the dispenser above; the next observer below watches it |

Replace the original dispenser `(22,F,22)` with full stone. Remove the original observer `(22,F+2,22)`. Dust needs support at `(23,F-1,22)`; all but the lowest one stand on the observer of the next floor down. Add a full stone support at `(23,33,22)` for the lowest dust.

Enclose **all four sides** of every dust cell with full blocks: `(22,F,22)`, `(24,F,22)`, `(23,F,21)`, `(23,F,23)`. The dispenser is its roof. This avoids relying on whether neighboring top slabs can admit secondary-layer water in Bedrock.

The mechanism does not observe water, dispenser `triggered` state, or analog container contents. The native electrical path is `observer → strong-powered full dispenser → dust below → next observer`. A normal pulse's rise and fall are covered by the next observer's scheduled pulse window; the references do not imply an extra falling-edge pulse for every stage. This is component reasoning supported by the pinned third-party implementation, not a BDS runtime test.

## Final 14-observer clock feed

Relocate the old roof observer to `(23,74,22)` facing WEST, looking at the original comparator `(22,74,22)`. Its EAST output directly powers full stone `(24,74,22)`.

- Dust A `(24,74,23)`, support full stone `(24,73,23)`.
- Dust B `(23,73,23)`, support full stone `(23,72,23)`.
- Dust C `(23,73,22)`, support top observer `(23,72,22)`.

Leave `(23,74,23)` **air**, because it is the clearance above B for the A→B step down. Relocate the lever to `(21,74,23)`, wall-mounted facing WEST, still attached to the original stone `(22,74,23)`. This preserves the original control input. Dust C can connect horizontally while underneath the relocated clock observer.

An opaque roof at y73 can retain bottom-slab spawn-proof caps at y74, excluding the above circuitry and its clearance. Exact roof lighting and spawning were delegated separately; this audit validates the specified redstone clearances only.

The existing 16-item clock has about 6.4 seconds per hopper-transfer leg (16×8 game ticks), before piston/scheduling overhead. Detecting comparator on/off gives approximately one column pulse per leg; the bucket therefore alternates wet/dry at that interval. Exact native timing has not been run here. The column has no long analog dust chain: each observer regenerates the signal.

For a dry startup, clear only the 13 flush sources/flows and give each relocated dispenser exactly one water bucket. Keep the collection basin water. Pause the clock while setting the state consistently; never mix empty and full buckets or add other dispenser items.

## Lateral-dispenser water shadow

The central diamond is radius7. The relocated dispenser blocks the first EAST cell `(23,F+1,22)`. A planar shortest-path check shows that offsets `(6,0)` and `(7,0)` from the source now need 8 and 9 steps, exceeding water's horizontal range7. They are cells `(28,F,22)` and `(29,F,22)` on each floor (26 cells total).

Prefer removing those two floor cells to air: this eliminates dry spawn/landing surfaces and preserves a fall path outside every lower platform. Changing them to BOTTOM slabs prevents spawning on those slabs and preserves the footprint/material, but does not guarantee flushing mobs that fall or walk onto them from neighboring cells or upper floors. Do not call that variant fully flushed. The distance check is a necessary upper-bound calculation, not the game's preferential fluid-flow algorithm.

## Evidence and limits

`audit_geometry.py` checks the original 13 floors/sources, direct neighbor relationships, dust supports, final three-dust clock path, the closed gate, the clear shaft, and the lateral distance obstruction. `geometry-check.txt` is its output; `geometry-audit.json` records exact coordinates and source hash.

Pinned PowerNukkitX commit `37caa9e66690f2d327b33d8a72e7621377ab96c5` supports the component reasoning:

- `BlockDispenser` extends `BlockSolid` with ordinary solid definition, no transparency/power-source override.
- `Block.isNormalBlock()` accepts solid/nontransparent/non-power-source blocks.
- `BlockRedstoneWire.getIndirectPower()` reads all six neighbors and reads strong inputs through a normal block, including the dispenser above the wire.
- `BlockRedstoneWire.canBePlacedOn()` requires a solid upper support face, provided by the observer below.
- `BlockObserver` drives its output side; `RedstoneComponent.updateAroundRedstone(dispenser)` notifies all neighboring cells, including the dust below it.
- The dust's downward power does not feed back into the dispenser above it. Scheduled observer updates suppress reentry during its pending pulse.

These are third-party Bedrock implementation references, not Mojang BDS code. No universal Minecraft GUI, live world, native redstone engine, native fluid engine, or mob transport result is claimed.

## Final file review

The actual file `build/output/early_game_mob-farm_Bedrock_SD6.litematic`, SHA256 `909f4c588e7210b37b2c8f3ed4dc1bf4b6931bf1b41c8a1a1e23a81354803851`, was independently decoded with nbtlib and a separate palette-bit decoder in `audit_final.py`. No builder functions were imported. Its 45×103×45 enclosure includes the added AFK structure; the original farm's 77-block vertical span and 13 floors remain.

All specified modules, dry electrical enclosures, lowest support, 14-observer clock route, mandatory descent clearance, relocated lever, opaque roof/caps, dry water-bucket startup, clock16cobblestone,25 open gates,31 air shaft layers,25 empty hoppers, two empty chests, and AFK pad/headroom passed direct NBT assertions. The two dry EAST tips are **AIR** in this file, resolving the previously documented bottom-slab-perch limitation.

Every floor now has111 remaining floor cells in its original diamond. Excluding the cell occupied by the lateral dispenser at water height leaves110 surfaces. A separate shortest-path computation with that dispenser as a blocker found every remaining surface within seven horizontal steps of the source. This establishes the distance constraint; it still does not execute Minecraft's preferential fluid flow or prove mob transport.

A standalone FIFO component model uses a single pending observer update, delayed rising edge, two-game-tick output pulse, instantaneous dust conduction through the dispenser, and the dispenser's four-game-tick activation delay. An isolated clock edge produces exactly13 dispenser activations, one in every floor. Four clock edges128 ticks apart produce exactly four activations per dispenser, no extras, and a drained event queue. The pinned PNX observer's two-tick delay and a four-tick sensitivity variant both pass, as do two insertion orders for coincident off/downstream events. The final activation latency is32 ticks in the two-tick model,60 in the four-tick variant, both less than the128-tick clock spacing. These are explicit mathematical component models, **not BDS or the full PNX world engine**.

`final-audit.json` and `final-check.txt` contain the final results pinned to the above file hash. The input and final artifact were read only throughout this review.
