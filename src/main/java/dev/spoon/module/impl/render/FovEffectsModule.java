package dev.spoon.module.impl.render;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.NumberSetting;

public final class FovEffectsModule extends Module {

    private final NumberSetting intensity = registerSetting(
            new NumberSetting(
                    "Intensity %",
                    0.0D,
                    0.0D,
                    100.0D,
                    1.0D
            )
    );

    public FovEffectsModule() {
        super(
                "FOV Effects",
                "Controls dynamic FOV effect intensity",
                ModuleCategory.RENDER,
                Module.UNBOUND_KEY
        );
    }

    public float applyIntensity(float vanillaModifier) {
        if (!isEnabled()) {
            return vanillaModifier;
        }

        float intensityMultiplier =
                (float)(intensity.getDoubleValue() / 100.0D);

        return 1.0F
                + (vanillaModifier - 1.0F)
                * intensityMultiplier;
    }

    public double getIntensityPercentage() {
        return intensity.getDoubleValue();
    }
}