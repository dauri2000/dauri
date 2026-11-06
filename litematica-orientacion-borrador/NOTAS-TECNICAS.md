# DRAFT: native Bedrock placement for EasyPlaceFix

This unreleased source stage composes the ownership/queue and support/slab policies against the genuine supplied Minecraft 26.3 client. It is based on the exact uploaded EasyPlaceFix 0.6.7 custom fork. No mod JAR is packaged or published. Original inputs and earlier source stages remain preserved separately.

The video establishes schematic smooth-stone slab `type:top` versus client `type:bottom`; it does not establish displacement or authoritative BDS state. Exact IF54 code can replace an AIR-destination EasyPlaceFix hit with the vanilla crosshair hit without preserving requested face/height. The new path checks original and fallback hits with the same half policy and never forwards a rejected hit through that restore.

## Implemented source

- Explicit relative coordinates become world coordinates exactly once. Selected output is an ordinary absolute BlockHitResult; MISS, border and mismatched native hits are rejected.
- The adapter checks loaded chunks, performs bounded real outline rays and constructs context from the actual first hit. It invokes virtual native item placement/permission, including subclass behavior, and applies legitimate BLOCK_STATE components immutably for preview. It never writes a preview state or manufactures an acknowledgement.
- Slab TOP/BOTTOM, first-half/double merge, intermediate water and same-cell merge have explicit policies. Upper vertical halves and bed heads resolve through the schematic lower/foot anchor. The support/visibility checks are a conservative deterministic-destination policy, not proof of universal BDS restrictions on floating or occluded clicks.
- A genuine projected context supplies the planned wire look/secondary use to normal item placement without moving the camera. Native scope excludes the old EasyPlaceFix state-return overrides; the Java path retains them.
- A Bedrock-only FIFO is bounded to 512 active/waiting requests. Waiting jobs have no lease/TTL. A prepares before a normal auth interval and uses after the genuine END event; only then B prepares. Placement extras own a separate phase/lease. No AUTH or game tick is invented.
- Each native action removes only its owner, then restores the next prepared owner or normal camera/physical input. Native Bedrock restores direction automatically; Java retains its existing configurable rotation-revert behavior.
- Movement rotation and planned true Shift are captured at packet construction. Input preserves raw true Shift and all six other bits. Encoders do not rewrite fields from live global state. Context refresh occurs only on the Minecraft thread; worker construction cannot change lifecycle state.
- Before lease and before use, requests recheck settings/protocol, player/world generation, schematic identity/state, render layer and active placement. Material goals are copied. Old queued requests cannot continue after disabling Easy Place or changing the schematic.
- Support policy identifies actual interaction overrides, including signs, and distinguishes same-cell stacking. Note tuning retains its job/count and retries while the native interval is busy. Countdown rejection rolls back only its reservation.

## Verification and build status

The combined pure suites pass 716 assertions: ownership, context/admission, geometry/slab policy and FIFO. These are not game placements.

Independent genuine-Minecraft checks include 57 slab/context cases, 26 canonicalization cases, 79 projected-context/previews, 12 registry/state checks, interaction-class checks, thin-shape refinement and 256 input combinations with codec roundtrips. Worlds/players are explicitly mocked where required; none proves the full live ray/adapter or applied Mixins.

The actual FIFO source was connected to genuine ViaVersion/ViaBedrock handlers: AUTH_A precedes USE_A and AUTH_B precedes USE_B with exactly two normal supplied ticks. This is a constructed protocol fixture, not a running server.

Run `./run-pure-tests.sh` for pure checks and `python3 compile-native.py` for all changed main sources. The latter uses genuine supplied client/libraries and `-proc:none -Xlint:all`; it does not package a JAR. Full compilation currently remains blocked by authentic Fabric Loader 0.19.5 API: net.fabricmc.api.ClientModInitializer is required by the original EasyPlaceFix class. No fake API or source-built substitute is used. Native subsets compile with genuine MC/Sponge. Compile-only MixinExtras 0.5.0 is not certified as the actual Loader bundle.

Actual Fabric/Mixin transformation and running Minecraft/BDS validation remain pending. A genuine Loader binary is the next build input; full game execution also requires its actual libraries/natives/assets. Authoritative tests require genuine BDS 1.26.60.6. Do not describe all types or states as passed in game.

## Explicit limits

- Potted plants need an intermediate empty pot and a separately owned follow-up. They currently reject instead of running the old direct callback incorrectly.
- LOOSEN_MODE substitution into a different block type is unsupported by this native preview; user preferences are not overwritten.
- Thin/partial shapes and other special interactions remain subject to conservative checks. Legitimate item BLOCK_STATE components are preserved; preview includes their effect after the normal native permission check, without adding a second post-component canPlace that vanilla does not perform. The comparator checks selected placement/orientation, attachment-mask and water properties; it does not compare every property or simulate later neighbor/block-entity effects.
- A manual use outside FIFO before the next normal camera AUTH is unverified. Native cleanup sends normal look restoration; immediate manual gameplay timing is not certified.
- No running full mod combination, applied constructor hooks or authoritative BDS placements have passed. No orientation download is ready from this directory.

Required settings remain Litematica Easy Place mode and EasyPlaceFix enabled, easyPlacePostRewrite=false, and effective SLAB_ONLY. The user confirmed EasyPlaceFix enabled; its default false is not asserted as this incident's cause.

The original MIT license is preserved in LICENSE_EasyPlaceFix. Minecraft binaries, decompiled client trees, recordings and private fixture logs are excluded from a source-only repository draft.
