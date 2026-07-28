package dev.spoon.module.impl.player;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.NumberSetting;

public final class CombatReachModule extends Module {

    private final NumberSetting distance = registerSetting(
            new NumberSetting(
                    "Distance",
                    4.0D,
                    3.0D,
                    6.0D,
                    0.1D
            )
    );

    public CombatReachModule() {
        super(
                "Combat Reach",
                "Increases player reach in combat",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    public double getDistance() {
        return distance.getDoubleValue();
    }

    public double getEffectiveDistance(double vanillaDistance) {
        return isEnabled() ? getDistance() : vanillaDistance;
    }
}