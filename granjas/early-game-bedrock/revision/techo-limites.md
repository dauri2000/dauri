# Roof lighting: bounded evidence and remaining uncertainty

Requested public routes were tried once each: mcwiki.gg/wiki/Slab, Minecraft Wiki raw, Fandom raw and minecraft.wiki.gg. All returned environment-proxy CONNECT403; `web-probes.json` records the results. A public GitHub issue search succeeded but returned no relevant authoritative Bedrock slab-roof evidence (`github-probe.json`). These failures are not evidence about the block's behavior.

PowerNukkitX treats BlockSlab as transparent and its simplified skylight path does not establish that this roof is opaque in vanilla Bedrock. Its light algorithm must not be substituted for BDS's engine/shape-occlusion rules. No direct BDS test or reliable public engine definition for the original bottom-slab roof was obtained.

Therefore the original roof cannot honestly be called broken, and replacing it cannot be presented as a demonstrated edition fix. Two reviewable choices remain:

- Preserve the original bottom-slab roof and mark Bedrock darkness as an explicit unverified requirement.
- For a conservative adaptation, use full opaque blocks at the same roof footprint/Y73, and bottom slabs above atY74 where no existing/new circuit occupies the cell. This preserves the two-block clear space above the highest platform atY70 and prevents ordinary roof-top spawning. It changes materials for reliability; it is not proof that the original slabs fail.

The original roof footprint is the Manhattan diamond `abs(x-22)+abs(z-22)<=22` atY73. It contains1013 cells:1011 bottom slabs plus the central clock-support/detector exceptions. Any upper spawnproofing must respect the final clock/bus rather than blindly filling everyY74 cell. The proposed AFK glass floorY99 and coverY102 are separate from this chamber roof.
