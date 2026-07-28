package dev.spoon.gui.clickgui;

import java.util.List;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public final class CategoryPanel {

    private static final int HEADER_HEIGHT = 18;
    private static final int MODULE_HEIGHT = 16;

    private static final int HEADER_COLOR = 0xEE222222;
    private static final int HEADER_HOVER_COLOR = 0xEE2D2D2D;

    private static final int MODULE_COLOR = 0xDD171717;
    private static final int MODULE_HOVER_COLOR = 0xDD252525;
    private static final int MODULE_ENABLED_COLOR = 0xDD3A3A3A;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;

    private final Minecraft mc = Minecraft.getMinecraft();

    private final ModuleCategory category;
    private final List<Module> modules;

    private int x;
    private int y;
    private int width;

    private boolean expanded = true;

    public CategoryPanel(
            ModuleCategory category,
            ModuleManager moduleManager
    ) {
        this.category = category;
        this.modules = moduleManager.getByCategory(category);
    }

    public void draw(int mouseX, int mouseY) {
        boolean headerHovered = isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                HEADER_HEIGHT
        );

        Gui.drawRect(
                x,
                y,
                x + width,
                y + HEADER_HEIGHT,
                headerHovered
                        ? HEADER_HOVER_COLOR
                        : HEADER_COLOR
        );

        String categoryName = category.getDisplayName();

        mc.fontRendererObj.drawStringWithShadow(
                categoryName,
                x + 5,
                y + centerTextOffset(HEADER_HEIGHT),
                TEXT_COLOR
        );

        String indicator = expanded ? "-" : "+";

        mc.fontRendererObj.drawStringWithShadow(
                indicator,
                x + width
                        - mc.fontRendererObj.getStringWidth(indicator)
                        - 5,
                y + centerTextOffset(HEADER_HEIGHT),
                TEXT_COLOR
        );

        if (!expanded) {
            return;
        }

        int moduleY = y + HEADER_HEIGHT;

        for (Module module : modules) {
            boolean hovered = isInside(
                    mouseX,
                    mouseY,
                    x,
                    moduleY,
                    width,
                    MODULE_HEIGHT
            );

            int backgroundColor;

            if (module.isEnabled()) {
                backgroundColor = MODULE_ENABLED_COLOR;
            } else if (hovered) {
                backgroundColor = MODULE_HOVER_COLOR;
            } else {
                backgroundColor = MODULE_COLOR;
            }

            Gui.drawRect(
                    x,
                    moduleY,
                    x + width,
                    moduleY + MODULE_HEIGHT,
                    backgroundColor
            );

            mc.fontRendererObj.drawStringWithShadow(
                    module.getName(),
                    x + 6,
                    moduleY + centerTextOffset(MODULE_HEIGHT),
                    module.isEnabled()
                            ? TEXT_COLOR
                            : DISABLED_TEXT_COLOR
            );

            moduleY += MODULE_HEIGHT;
        }
    }

    /**
     * @return true when this panel handled the click
     */
    public boolean mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        if (isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                HEADER_HEIGHT
        )) {
            /*
             * Left click or right click on the category header toggles
             * the dropdown.
             */
            if (mouseButton == 0 || mouseButton == 1) {
                expanded = !expanded;
                return true;
            }
        }

        if (!expanded) {
            return false;
        }

        int moduleY = y + HEADER_HEIGHT;

        for (Module module : modules) {
            if (isInside(
                    mouseX,
                    mouseY,
                    x,
                    moduleY,
                    width,
                    MODULE_HEIGHT
            )) {
                if (mouseButton == 0) {
                    module.toggle();
                    return true;
                }

                /*
                 * Reserve right click for opening module settings later.
                 */
                if (mouseButton == 1) {
                    return true;
                }
            }

            moduleY += MODULE_HEIGHT;
        }

        return false;
    }

    private int centerTextOffset(int rowHeight) {
        return (rowHeight - mc.fontRendererObj.FONT_HEIGHT) / 2;
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int elementX,
            int elementY,
            int elementWidth,
            int elementHeight
    ) {
        return mouseX >= elementX
                && mouseX < elementX + elementWidth
                && mouseY >= elementY
                && mouseY < elementY + elementHeight;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}