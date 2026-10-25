# Source and contract review

Final reviewed scope is one replacement `CraftingDataTracker.class` and one added `FireworkMultiRecipes.class`. The matcher adds an adapter to its returned match, retaining every original branch and all other methods. The helper only acts in the player/table crafting station for an exact current native MULTI UUID `00000000-0000-0000-0000-000000000002` with nonzero announced network ID. It creates a per-grid ephemeral SHAPELESS view so the unchanged existing output code can consume the exact native inputs and send the server's own recipe identity.

The native MULTI UUID meaning and recipe count/flight relationship are corroborated by the owner's pinned protocol/third-party references. The public recipe dump is a reference fixture, not this user's actual session. The independent wire probe deliberately uses different network IDs so public example IDs cannot accidentally pass.

Vanilla cardinality is one occupied paper slot, one to three occupied gunpowder slots and optional occupied firework-star slots within the actual grid capacity. Each occupied slot contributes one ingredient per craft, irrespective of stack size. Unrelated ingredients preserve original matching. Explicit original star results and original custom recipes outside vanilla rocket cardinality remain unchanged. Validated native explosion tags are deep-copied to the rocket result; no incoming stack tag is rewritten globally.

No cause for broad changes to item equality, inventory transactions, bulk iteration, ACKs, request timeouts or synchronization was found in this bounded fix. Existing success handling intentionally retains its full-NBT inventory-refresh gate for crafted tagged items. The tests verify that gate rather than bypass it. In-game timing and whether the user's server supplies the expected authoritative updates remain untested.

No release blocker found within the stated vanilla rocket scope. Broader native dynamic recipes, star creation/fading and unusual custom star NBT are separate work and are not certified by this review.
