package com.viaversion.viafabricplus.bedrock.injection.mixin.compat.easyplacefix;

import com.viaversion.viafabricplus.bedrock.compat.BedrockMaterialComparison;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Normalize validated Bedrock transport metadata only for a material lookup. */
@Pseudo
@Mixin(targets = "org.uiop.easyplacefix.util.PlacementInventory", remap = false)
public abstract class MixinEasyPlaceFixMaterialSearch {
    @Redirect(
        method = "getSlotWithStack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Inventory;)I",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            remap = false
        ),
        remap = false,
        require = 1,
        allow = 1
    )
    private static boolean bedrock$compareMaterial(ItemStack required, ItemStack available) {
        if (!ItemStack.isSameItem(required, available)) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(
            BedrockMaterialComparison.normalizeCopy(required),
            BedrockMaterialComparison.normalizeCopy(available)
        );
    }
}
