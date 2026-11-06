package org.uiop.easyplacefix.placement;

/** Secondary use is selected before preview and preserves required input masks. */
public final class NativeSecondaryUsePolicy {
    private NativeSecondaryUsePolicy() {}

    public static boolean[] candidates(boolean physicalSecondary, boolean supportRequiresSecondary) {
        return physicalSecondary || supportRequiresSecondary ? new boolean[]{true} : new boolean[]{false,true};
    }
}
