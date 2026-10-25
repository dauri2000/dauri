# Independent review: IF49 native-container preflight reuse

The frozen candidate `BedrockNativeContainerScreen.class` passed this review without edits or rebuilding. Its SHA256 is `902b8b6b4a99ffd887bc21f8f0364e14cb5ed2494e5912842c631ca3c009e57d`.

## Scope and static checks

An independent ASM reader compared the candidate directly with the class inside the delivered IF48 JAR. It removed only the declared five private fields, one private helper and the additions to `prepare`, `init` and `discard` in memory. Exact canonical equality with IF48 followed. Every full-class method also passed `BasicVerifier`.

The helper consumes the one-shot marker and releases its cached Font/title references before checking any changing input. Its only field writes are to those three fingerprint fields. The original superclass initialization and left/top origin setup happen before the reuse guard. A failed guard preserves the exact original renderer-close, renderer-create and refresh instructions.

`prepare` invalidates any earlier token before work and arms the new fingerprint only after successful original refresh. `discard` invalidates the token before the original cleanup. The unchanged `removed` method calls `discard` from its existing finally path. Existing input handling, activation, server actions, inventory, animation, refresh/layout and fallback bodies remain unchanged by canonical reversal.

The guard checks ownership, renderer presence, inactive activation state, Font identity, both dimensions, title text, slot count, all slot values, carried value, gamerule view and image revision. This is a single reuse of the already successful preflight. Later initializations, including resize, take the original path.

## Additional executed edge cases

Six independent groups execute the four sliced production methods:

1. No preflight: first and repeated initialization use the original refresh and cleanup path.
2. A realistic preflight before `Screen.font` is assigned: after the client assigns the same Font and dimensions, reuse succeeds. The fingerprint correctly retains the `prepare` argument.
3. An unchanged full 90-slot menu consumes its preflight without a second refresh or renderer close.
4. A type mismatch at the final slot is detected. Restoring that item cannot revive the consumed token for another initialization.
5. In-place carried-item component mutation triggers refresh, without relying on object identity.
6. If a mismatched initialization reaches the original refresh and that refresh throws, the token remains consumed; a later recovery performs the original refresh.

Four bytecode inspection groups plus these six execution groups give **10 independent groups**. The owner's 18 groups were reviewed and are not counted again.

## Explicit limits

The test host contains exact canonical copies of the candidate's `prepare`, `init`, `discard` and new helper bodies; the independent checker verifies that equality. It is not the complete screen class. Heavy `refresh`, `current` and `showTags` are explicit instrumentation boundaries supplying snapshots, call counts and faults. Minecraft superclass, Font, item/menu/slot and UI image/renderer APIs are test-only boundaries. The real parser, layout, drawing, GPU and server do not execute in these control-flow checks.

No full Minecraft client linkage, Mixin transformation, gameplay or user frame-time measurement occurred. The fix removes one redundant rebuild along the guarded opening path. The first validated preflight still runs, so this review does not promise removal of every possible cause of chest lag or a particular FPS improvement.

`verification.json` records the candidate, baseline, sources, host and evidence hashes. No production files were edited or published by this reviewer.
