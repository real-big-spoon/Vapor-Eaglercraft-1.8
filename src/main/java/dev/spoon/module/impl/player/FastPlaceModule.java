package dev.spoon.module.impl.player;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.NumberSetting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class FastPlaceModule extends Module {

    /*
     * Vanilla block placement delay is normally 4 ticks.
     *
     * 0 = no additional client delay
     * 4 = vanilla
     * 5 = slightly slower than vanilla
     */
    private final NumberSetting delayTicks = registerSetting(
            new NumberSetting(
                    "Delay Ticks",
                    0.0D,
                    0.0D,
                    5.0D,
                    1.0D
            )
    );

    public FastPlaceModule() {
        super(
                "Fast Place",
                "Changes the delay between held block placements",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    public int resolvePlacementDelay(int vanillaDelay) {
        if (!isEnabled()) {
            return vanillaDelay;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            return vanillaDelay;
        }

        /*
         * Keep the module restricted to integrated singleplayer.
         */
        if (!mc.isSingleplayer()) {
            return vanillaDelay;
        }

        ItemStack heldItem = mc.thePlayer.getHeldItem();

        /*
         * Do not affect food, bows, buckets, doors, or other non-block
         * right-click actions.
         */
        if (heldItem == null
                || !(heldItem.getItem() instanceof ItemBlock)) {
            return vanillaDelay;
        }

        return getDelayTicks();
    }

    public int getDelayTicks() {
        int value = (int)Math.round(
                delayTicks.getDoubleValue()
        );

        return Math.max(0, Math.min(5, value));
    }
}