package org.uiop.easyplacefix.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.util.PlayerBlockAction;
import org.uiop.easyplacefix.placement.NativePlacementScope;

@Mixin(PistonBaseBlock.class)
public class MixinPistonBlock {
   @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
   private BlockState ModgetPlacementState(BlockState original) {
      if (NativePlacementScope.isActive()) return original;
      if (PlayerBlockAction.useItemOnAction.modifyBoolean
         && original != null
         && PlayerBlockAction.useItemOnAction.pistonBlockState != null
         && Minecraft.getInstance().isSameThread()) {
         PlayerBlockAction.useItemOnAction.modifyBoolean = false;
         return PlayerBlockAction.useItemOnAction.pistonBlockState.setValue(BlockStateProperties.EXTENDED, false);
      } else {
         return original;
      }
   }
}
