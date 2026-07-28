package dev.spoon.module.impl.hud;

import net.minecraft.client.gui.ScaledResolution;
import dev.spoon.VaporClient;
import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;

public final class ModDataModule extends Module {

    public ModDataModule() {
        super(
                "Show Mod Data",
                "Displays the client name and version",
                ModuleCategory.HUD,
                -1 // keycode none
        );

        setEnabled(true);
    }

    @Override
    public void onRender2D(float partialTicks) {
//        mc.fontRendererObj.drawStringWithShadow(
//                VaporClient.NAME + " " + VaporClient.VERSION,
//                4.0F,
//                4.0F,
//                0xFFFFFFFF
//        );
        ScaledResolution resolution = new ScaledResolution(mc);

        String text = VaporClient.NAME + " " + VaporClient.VERSION;

        int margin = 4;
        int x = resolution.getScaledWidth()
                - mc.fontRendererObj.getStringWidth(text)
                - margin;

        int y = resolution.getScaledHeight()
                - mc.fontRendererObj.FONT_HEIGHT
                - margin;

        mc.fontRendererObj.drawStringWithShadow(
                text,
                x,
                y,
                0xFFFFFFFF
        );
    }
}