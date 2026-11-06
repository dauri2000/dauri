# DRAFT: native Bedrock placement for EasyPlaceFix

This unreleased source stage modifies the exact supplied EasyPlaceFix 0.6.7 fork for Minecraft Java 26.3 and BDS 1.26.60.6. No installable orientation JAR has been packaged or published. Originals and earlier source stages are preserved. Read the current checkpoint and receipts before reusing a result.

## Implemented source

- Relative hit coordinates become absolute coordinates once. A bounded selector checks actual support rays, loaded chunks, reach, native item context and virtual placement preview. No desired block state or server acknowledgement is manufactured.
- Planned yaw/pitch and secondary use are projected into native context without moving the camera. Placement properties, axes, halves and water are compared; slab placement/merging has separate policies. Legacy state-return and piston overrides are excluded inside native scope.
- The user confirmed easyPlacePostRewrite=true. A new bridge reuses POST target selection, including its position override, before material selection/cache/direct use. Both engines use the native path on Bedrock with effective SLAB_ONLY. Java keeps its original dispatch; the preference is not changed. Requests capture the selected engine so switching it cancels stale work.
- The bounded FIFO gives waiting jobs no active angle lease. A second phase now requires BOTH a later observed ordinary CLIENT_TICK_END send and a later END callback. A Minecraft hook observes the genuine send for the current connection; it does not send AUTH, invent ticks or execute a placement. This closes the actual MaLiLib RETURN-before-Fabric END window, where an END-only delay could use the previous auth orientation.
- Per-packet immutable angles and planned true Shift are captured at construction. Physical true Shift and the other six input flags remain intact. Context changes invalidate jobs; only the main thread observes lifecycle identity. Cleanup releases only its owner and restores pending/native camera input.
- Chest type and ceiling-hanging-sign attachment are checked. Secondary use is selected before preview, without forcing physical Shift off. A first SINGLE chest half is allowed only with a loaded, reciprocal, matching schematic partner and an absent/replaceable real partner. Derived tripwire attachment is not treated as a hanging-sign property.
- Filled pots use an empty-pot placement goal while retaining the original schematic for cancellation. A later activation fills an observed empty pot with the exact plant through its own FIFO action. The old potted callback is not invoked; existing occupied pots are not automatically replaced.
- Existing support interaction, note-tuning retry, queue admission, schematic/layer/world/hand guards and input ownership remain scoped to their actions.

## Executed checks and limits

The current 35 changed main Java sources compile with genuine supplied Minecraft26.3, Loader0.19.5, its nested MixinExtras Fabric0.5.5 and the restored dependencies. The final javac receipt records exit0 and no diagnostics. Exact runtime transformation dependencies include ASM9.10.1. Superseded compile-only Extras0.5.0 and ASM9.9.1 are not used for transformation.

Individual pure suites cover ownership581, lifecycle18, admission29 and support-policy57 assertions. The updated FIFO suite has35 assertions, including preparation after an auth observation, END without another auth, and notification without END. They are component tests, not block placements.

Separate genuine Minecraft checks include57 slab/context cases,26 canonicalization checks,79 projected-context/previews and256 input-mask/codec combinations. Targeted secondary-state checks add48 checks; replaying the unchanged original23-check witness rejects its four formerly accepted wrong-state pairs. A separate pot fixture calls the genuine block-use route for all39 filled pot types plus two controls,41 cases: the mocked world observes the exact potted state and the real stack consumes one item. Player/Level mocks and unexecuted adapter/raycast paths are declared in the receipts.

The real queue and genuine ViaVersion/ViaBedrock handlers reproduce the stale-auth window. The corrected fixture waits through the first END, then uses only after the next supplied normal auth interval. Constructed packets/ticks and an in-memory channel are explicit test adapters, not a live BDS session.

Actual Sponge/Knot-service transformation was executed on the complete EPF configuration in an explicitly declared offline launcher harness. See /workspace/litematica-work/mixin-transform and the auth-boundary review for the current snapshot, combined MaLiLib/Fabric event-order check and exact counts. Earlier POST-only results do not automatically certify a later hook. The checked input record passed structural/hierarchy checks and JVM define/resolve verification; its constructor was not executed by that proof. JAVA_25 matches class69; the exact framework retains a generic MAX_SUPPORTED13 warning.

Commands:

```sh
./run-pure-tests.sh
python3 compile-native.py
```

Do not repeat unchanged passing suites without a new concern. Compare current source/class/config hashes to their receipts before reusing evidence. Neither command starts Minecraft/BDS or creates a release JAR.

## Still pending

No complete Minecraft/modpack launch or authoritative BDS placement test has passed. The real registry audit covers1286 identifiers, but contains zero placements. It does not prove every applicable type or property works.

Pending work includes authoritative geometry/orientation/inventory tests; dry-to-waterlogged staging; candle-cake and filled-cauldron interactions; growth/head/body transitions; quantity and post-interaction completion; LOOSEN cross-block substitutions; and immediate manual use before the next restored camera auth. Some final states depend on neighbors or world mechanics rather than the first placement preview. All raw-property differences must not be treated as initial placement failures.

Five Mojang game libraries, assets/native setup and the genuine BDS1.26.60.6 package remain separate full-game/server prerequisites. Old upload IDs for those missing files could not be resolved. A saved official-domain network draft is not applied access; follow supported environment review and checksum verification rather than bypassing policy.

Required settings are Easy Place mode and EasyPlaceFix enabled with effective SLAB_ONLY on Bedrock. Both values of easyPlacePostRewrite are supported by the source bridge. No option is silently changed.

The original MIT license is preserved in LICENSE_EasyPlaceFix. Minecraft binaries, decompiled client trees, user recordings/logs/worlds and private fixture logs are excluded from the repository source draft. No orientation download is ready.
