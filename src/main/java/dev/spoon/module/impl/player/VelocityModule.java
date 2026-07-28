package dev.spoon.module.impl.player;

import dev.spoon.event.VelocityEvent;
import dev.spoon.event.VelocitySource;
import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.BooleanSetting;
import dev.spoon.setting.NumberSetting;

public final class VelocityModule extends Module {

    /*
     * Reduction percentage:
     *
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
     * Reduction percentage:
     *
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

    /*
     * false = explosion knockback remains vanilla
     * true  = explosion knockback is also reduced
     */
    private final BooleanSetting includeExplosions = registerSetting(
            new BooleanSetting(
                    "Explosions",
                    false
            )
    );

    public VelocityModule() {
        super(
                "Velocity",
                "Reduces knockback applied to the player",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    @Override
    public void onVelocity(VelocityEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event.getEntity() != mc.thePlayer) {
            return;
        }

        if (!mc.isSingleplayer()) {
            return;
        }

        /*
         * Preserve normal explosion knockback when the setting is off.
         */
        if (event.getSource() == VelocitySource.EXPLOSION
                && !includeExplosions.isEnabled()) {
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

    public boolean includesExplosions() {
        return includeExplosions.isEnabled();
    }
}