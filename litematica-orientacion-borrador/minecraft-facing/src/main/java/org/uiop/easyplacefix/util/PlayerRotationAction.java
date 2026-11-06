package org.uiop.easyplacefix.util;

import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import org.uiop.easyplacefix.struct.DirectionRange;
import org.uiop.easyplacefix.rotation.RotationBoundPacket;

public class PlayerRotationAction {
   public static void setServerBoundPlayerRotation(Float yaw, Float pitch, Boolean hor) {
      Minecraft minecraftClient = Minecraft.getInstance();
      Rot packet = new Rot(yaw, pitch, Minecraft.getInstance().player.onGround(), hor);
      ((RotationBoundPacket) (Object) packet).easyplacefix$markExplicitRotation(yaw, pitch);
      minecraftClient.getConnection().send(packet);
   }

   public static void restRotation() {
      TickThread.releaseCurrentLookLock();
      Minecraft minecraftClient = Minecraft.getInstance();
      float yaw = minecraftClient.player.getYRot();
      float pitch = minecraftClient.player.getXRot();
      Rot packet = new Rot(yaw, pitch, minecraftClient.player.onGround(), minecraftClient.player.horizontalCollision);
      ((RotationBoundPacket) (Object) packet).easyplacefix$markExplicitRotation(yaw, pitch);
      minecraftClient.getConnection().send(packet);
   }

   public static Float limitYawRotation(Direction direction) {
      DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
      if (directionRange == null) {
         return null;
      }

      Direction playerFacing = Minecraft.getInstance().player.getMotionDirection();
      if (directionRange.isInRange(playerFacing)) {
         return Minecraft.getInstance().player.getYRot();
      }

      float range1 = Math.abs(directionRange.getFirstValue() - Minecraft.getInstance().player.getYRot());
      float range2 = Math.abs(directionRange.getSecondValue() - Minecraft.getInstance().player.getYRot());
      return range1 < range2 ? directionRange.getFirstValue() : directionRange.getSecondValue();
   }

   public static Float limitPitchRotation(Direction direction) {
      DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
      if (directionRange == null) {
         return null;
      }

      Direction playerFacing = getVertical(Minecraft.getInstance().player.getXRot());
      if (directionRange.isInRange(playerFacing)) {
         return Minecraft.getInstance().player.getXRot();
      }

      float range1 = Math.abs(directionRange.getFirstValue() - Minecraft.getInstance().player.getXRot());
      float range2 = Math.abs(directionRange.getSecondValue() - Minecraft.getInstance().player.getXRot());
      return range1 < range2 ? directionRange.getFirstValue() : directionRange.getSecondValue();
   }

   public static Direction getVertical(float pitchPlayer) {
      Direction playerFacing = null;
      if (pitchPlayer < -45.0F) {
         playerFacing = Direction.UP;
      } else if (pitchPlayer > 45.0F) {
         playerFacing = Direction.DOWN;
      }

      return playerFacing;
   }
}
