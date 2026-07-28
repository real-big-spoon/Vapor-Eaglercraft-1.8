package dev.spoon.module.impl.player;

import dev.spoon.event.VelocityEvent;
import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.NumberSetting;

public final class VelocityModule extends Module {

    /*
     * 0%   = vanilla horizontal velocity
     * 50%  = half horizontal velocity
     * 100% = no horizontal velocity
     */
    private final NumberSetting horizontalReduction = registerSetting(
            new NumberSetting(
                    "Horizontal %",
                    100.0D,
                    0.0D,
                    100.0D,
                    1.0D
            )
    );

    /*
     * 0%   = vanilla vertical velocity
     * 50%  = half vertical velocity
     * 100% = no vertical velocity
     */
    private final NumberSetting verticalReduction = registerSetting(
            new NumberSetting(
                    "Vertical %",
                    100.0D,
                    0.0D,
                    100.0D,
                    1.0D
            )
    );

    public VelocityModule() {
        super(
                "Velocity",
                "Reduce knockback applied to the player",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    @Override
    public void onVelocity(VelocityEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        /*
         * Only modify velocity applied to the local player.
         * Other entities must retain their normal velocity.
         */
        if (event.getEntity() != mc.thePlayer) {
            return;
        }

        /*
         * Remove this condition only if you deliberately want the module
         * active while connected to external multiplayer servers.
         */
        if (!mc.isSingleplayer()) {
            return;
        }

        double horizontalMultiplier = percentageToMultiplier(
                horizontalReduction.getDoubleValue()
        );

        double verticalMultiplier = percentageToMultiplier(
                verticalReduction.getDoubleValue()
        );

        event.setMotionX(
                event.getMotionX() * horizontalMultiplier
        );

        event.setMotionY(
                event.getMotionY() * verticalMultiplier
        );

        event.setMotionZ(
                event.getMotionZ() * horizontalMultiplier
        );
    }

    private double percentageToMultiplier(double reductionPercentage) {
        double clampedPercentage = Math.max(
                0.0D,
                Math.min(100.0D, reductionPercentage)
        );

        return 1.0D - clampedPercentage / 100.0D;
    }

    public double getHorizontalReduction() {
        return horizontalReduction.getDoubleValue();
    }

    public double getVerticalReduction() {
        return verticalReduction.getDoubleValue();
    }
}