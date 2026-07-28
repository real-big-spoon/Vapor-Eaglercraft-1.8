package dev.spoon.gui.clickgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.spoon.VaporClient;
import dev.spoon.module.ModuleCategory;
import net.minecraft.client.gui.GuiScreen;

public final class ClickGuiScreen extends GuiScreen {

    private static final int PANEL_WIDTH = 110;
    private static final int PANEL_GAP = 8;
    private static final int START_X = 12;
    private static final int START_Y = 18;

    private final List<CategoryPanel> panels = new ArrayList<>();

    private boolean initializedPanels;

    @Override
    public void initGui() {
        super.initGui();

        if (!initializedPanels) {
            createPanels();
            initializedPanels = true;
        }

        positionPanels();
    }

    private void createPanels() {
        panels.clear();

        for (ModuleCategory category : ModuleCategory.values()) {
            panels.add(
                    new CategoryPanel(
                            category,
                            VaporClient.getInstance().getModuleManager()
                    )
            );
        }
    }

    private void positionPanels() {
        int x = START_X;
        int y = START_Y;

        for (CategoryPanel panel : panels) {
            /*
             * Wrap onto another row if the next panel would go beyond
             * the right edge of the screen.
             */
            if (x + PANEL_WIDTH > width - START_X) {
                x = START_X;
                y += 150;
            }

            panel.setPosition(x, y);
            panel.setWidth(PANEL_WIDTH);

            x += PANEL_WIDTH + PANEL_GAP;
        }
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        /*
         * Remove this call if you do not want the world darkened behind
         * the ClickGUI.
         */
        drawDefaultBackground();

        for (CategoryPanel panel : panels) {
            panel.draw(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        VaporClient.getInstance()
                .getConfigManager()
                .save("default");

        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}