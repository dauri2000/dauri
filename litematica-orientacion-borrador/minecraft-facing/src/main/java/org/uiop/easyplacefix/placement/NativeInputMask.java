package org.uiop.easyplacefix.placement;

import java.util.Objects;
import net.minecraft.world.entity.player.Input;

/** Preserve real input; an active prepared action can only add its sneak bit. */
public final class NativeInputMask {
    private NativeInputMask() {}

    public static Input preserveShift(Input original, boolean plannedTrue) {
        Objects.requireNonNull(original,"original");
        if (!plannedTrue || original.shift()) return original;
        return new Input(original.forward(),original.backward(),original.left(),original.right(),
            original.jump(),true,original.sprint());
    }
}
