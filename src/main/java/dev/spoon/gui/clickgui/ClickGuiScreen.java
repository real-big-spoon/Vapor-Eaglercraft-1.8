package dev.spoon.gui.clickgui;

import java.util.ArrayList;
import java.util.List;

import dev.spoon.VaporClient;
import dev.spoon.module.ModuleCategory;
import net.minecraft.client.gui.GuiScreen;

public final class ClickGuiScreen extends GuiScreen {

    private static final int PANEL_WIDTH = 120;
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
            if (x + PANEL_WIDTH > width - START_X) {
                x = START_X;
                y += 170;
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
    protected void mouseClickMove(
            int mouseX,
            int mouseY,
            int clickedMouseButton,
            long timeSinceLastClick
    ) {
        for (CategoryPanel panel : panels) {
            panel.mouseDragged(
                    mouseX,
                    mouseY,
                    clickedMouseButton
            );
        }

        super.mouseClickMove(
                mouseX,
                mouseY,
                clickedMouseButton,
                timeSinceLastClick
        );
    }

    @Override
    protected void mouseReleased(
            int mouseX,
            int mouseY,
            int state
    ) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }

        super.mouseReleased(mouseX, mouseY, state);
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