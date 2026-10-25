# IF49 chest-lag audit: GUI items and texture/model preparation

No causal fix is proposed for the bounded GUI-item opacity hooks. In the delivered IF48 JAR these hooks do small state forwarding; the expensive native-screen presentation/layout work is being investigated separately. This does not exclude a problem in Minecraft's actual GUI item atlas, its cache, a driver, a resource pack, or an unrelated mod.

## Actual classes

There are no `BedrockNativeGuiItem*` classes in IF48. The relevant classes are `MixinNativeGuiItemExtraction`, `MixinNativeGuiItemState`, `MixinNativeGuiItemAtlas`, `MixinNativeGuiOversizedItem`, and `NativeInventoryItemOpacity`. All five are byte-identical to their IF34 originals; `verification.json` records their delivered IF48 hashes.

- Extraction stores a float opacity on the item render state and forwards the original `addItem` operation.
- The state mixin holds that float, initially `1.0F`.
- Atlas submission and oversized-item submission forward the original operation through the opacity helper.
- `NativeInventoryItemOpacity.apply(blit, 1.0F)` returns the identical incoming blit before any call or allocation. Lower opacity scales four color channels and constructs one replacement blit.
- These hooks contain no native-item NBT decoding, pack lookup, PNG decoding, model conversion, texture registration or resource reload. The ASM audit records every outgoing method call and allocation. It is a bounded call-site inspection, not a full transitive profile of Minecraft's implementations.

The target names describe extraction and render-state submission stages. Without running Minecraft, their actual thread placement is not measured here. No claim of render-thread safety or GPU performance is inferred from static names.

## Actual core probes

Three controlled checks execute IF48's unmodified core classes:

1. A fixture pack declares 90 different icon paths. The actual `CustomItemTextureResourceRewriter` builds their model definitions and copies all 90 PNGs without decoding any `BufferedImage`. The actual `Content.LazyImage.image` fields remain null after conversion.
2. 9000 calls to the actual `getItemModel` key builder produce stable IDs and perform zero additional source-pack reads or image requests. These are key-builder calls, not 9000 Minecraft GUI renders or cache measurements.
3. The actual lazy PNG byte fast path returns the original PNG bytes. A first explicit `getImage` decodes the image; a second call on the same `LazyImage` returns the identical cached image instance. Creating another `LazyImage` would have a separate decode cache.

No cold/warm FPS values are reported. This environment does not have the actual Minecraft GUI atlas or GPU runtime; timings from a small standalone Java probe would not establish the user's chest-frame cost.

## Conversion lifecycle

`ResourcePackRewriter.bedrockToJava` submits conversion suppliers to workers named `ViaBedrock ResourcePack Rewriter`. `ConvertedResourcePackCache` prepares conversions through its own executor and retains completed pack results under its size/count bounds. Resource-pack handlers invoke preparation when finishing a pack stack; the inspected chest/item opacity hooks do not invoke conversion. Disabling the conversion cache would change that lifecycle, so it cannot be assumed that every configuration reuses every pack forever.

Custom item model JSON and PNG generation therefore belong to pack preparation in the inspected code, not to the item opacity wrappers during each chest opening. Minecraft's own resource reload, baking, atlas allocation and texture upload were not executed and remain outside this audit.

`NativeInventoryItemPresentation` performs native NBT copying and hover-text work for native screens; the other client audit covers that path and screen refreshes. The video's similarity to an ordinary Java chest screen does not by itself identify which screen class is running.

No production classes, resources or package files were edited. The three probe groups and bytecode inspection are evidence for this scoped audit, not proof that chest opening is fixed.
