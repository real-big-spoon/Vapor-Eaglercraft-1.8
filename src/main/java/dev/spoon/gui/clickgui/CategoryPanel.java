package dev.spoon.gui.clickgui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.module.ModuleManager;
import dev.spoon.setting.BooleanSetting;
import dev.spoon.setting.NumberSetting;
import dev.spoon.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;

public final class CategoryPanel {

    private static final int HEADER_HEIGHT = 18;
    private static final int MODULE_HEIGHT = 16;
    private static final int SETTING_HEIGHT = 22;

    private static final int HEADER_COLOR = 0xEE202020;
    private static final int HEADER_HOVER_COLOR = 0xEE2A2A2A;

    private static final int MODULE_COLOR = 0xE6171717;
    private static final int MODULE_HOVER_COLOR = 0xE6242424;
    private static final int MODULE_ENABLED_COLOR = 0xE6353550;

    private static final int SETTING_COLOR = 0xE6121212;
    private static final int SETTING_HOVER_COLOR = 0xE61C1C1C;

    private static final int KEYBIND_LISTENING_COLOR =
            0xE6353550;

    private static final int SLIDER_BACKGROUND_COLOR =
            0xFF303030;

    private static final int SLIDER_FILL_COLOR =
            0xFF7777DD;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SECONDARY_TEXT_COLOR =
            0xFFAAAAAA;

    private static final int ENABLED_TEXT_COLOR =
            0xFFFFFFFF;

    private final Minecraft mc = Minecraft.getMinecraft();

    private final ClickGuiScreen owner;
    private final ModuleCategory category;
    private final ModuleManager moduleManager;

    /*
     * Stores whether each module's settings are expanded.
     *
     * The expanded area now always includes a keybind row, even when
     * the module has no ordinary settings.
     */
    private final Map<Module, Boolean> expandedModules =
            new HashMap<Module, Boolean>();

    private int x;
    private int y;
    private int width;

    private boolean expanded = true;

    /*
     * The NumberSetting currently being dragged.
     */
    private NumberSetting draggingSlider;

    /*
     * Bounds for the active slider, used while dragging.
     */
    private int draggingSliderX;
    private int draggingSliderWidth;

    public CategoryPanel(
            ClickGuiScreen owner,
            ModuleCategory category,
            ModuleManager moduleManager
    ) {
        if (owner == null) {
            throw new IllegalArgumentException(
                    "ClickGuiScreen cannot be null"
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "ModuleCategory cannot be null"
            );
        }

        if (moduleManager == null) {
            throw new IllegalArgumentException(
                    "ModuleManager cannot be null"
            );
        }

        this.owner = owner;
        this.category = category;
        this.moduleManager = moduleManager;
    }

    public void draw(int mouseX, int mouseY) {
        drawHeader(mouseX, mouseY);

        if (!expanded) {
            return;
        }

        int currentY = y + HEADER_HEIGHT;

        for (Module module : getModules()) {
            drawModuleRow(
                    module,
                    currentY,
                    mouseX,
                    mouseY
            );

            currentY += MODULE_HEIGHT;

            if (isModuleExpanded(module)) {
                currentY = drawExpandedModuleContents(
                        module,
                        currentY,
                        mouseX,
                        mouseY
                );
            }
        }
    }

    private void drawHeader(int mouseX, int mouseY) {
        boolean hovered = isInside(
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
                hovered
                        ? HEADER_HOVER_COLOR
                        : HEADER_COLOR
        );

        String title = category.getDisplayName();

        mc.fontRendererObj.drawStringWithShadow(
                title,
                x + 5,
                y + centerTextOffset(HEADER_HEIGHT),
                TEXT_COLOR
        );

        String indicator = expanded ? "-" : "+";

        mc.fontRendererObj.drawStringWithShadow(
                indicator,
                x + width
                        - mc.fontRendererObj
                        .getStringWidth(indicator)
                        - 5,
                y + centerTextOffset(HEADER_HEIGHT),
                TEXT_COLOR
        );
    }

    private void drawModuleRow(
            Module module,
            int moduleY,
            int mouseX,
            int mouseY
    ) {
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
                        ? ENABLED_TEXT_COLOR
                        : SECONDARY_TEXT_COLOR
        );

        /*
         * Every module can now be expanded because every module has
         * a keybind row, even if it has no normal settings.
         */
        String indicator = isModuleExpanded(module)
                ? "-"
                : "+";

        mc.fontRendererObj.drawStringWithShadow(
                indicator,
                x + width
                        - mc.fontRendererObj
                        .getStringWidth(indicator)
                        - 5,
                moduleY + centerTextOffset(MODULE_HEIGHT),
                SECONDARY_TEXT_COLOR
        );
    }

    private int drawExpandedModuleContents(
            Module module,
            int currentY,
            int mouseX,
            int mouseY
    ) {
        /*
         * Keybind is displayed before the module's normal settings.
         */
        drawKeybindRow(
                module,
                currentY,
                mouseX,
                mouseY
        );

        currentY += SETTING_HEIGHT;

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                drawNumberSetting(
                        (NumberSetting)setting,
                        currentY,
                        mouseX,
                        mouseY
                );
            } else if (setting instanceof BooleanSetting) {
                drawBooleanSetting(
                        (BooleanSetting)setting,
                        currentY,
                        mouseX,
                        mouseY
                );
            } else {
                drawUnsupportedSetting(
                        setting,
                        currentY,
                        mouseX,
                        mouseY
                );
            }

            currentY += SETTING_HEIGHT;
        }

        return currentY;
    }

    private void drawKeybindRow(
            Module module,
            int settingY,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                settingY,
                width,
                SETTING_HEIGHT
        );

        boolean listening =
                owner.isCapturingKeybind(module);

        int backgroundColor;

        if (listening) {
            backgroundColor = KEYBIND_LISTENING_COLOR;
        } else if (hovered) {
            backgroundColor = SETTING_HOVER_COLOR;
        } else {
            backgroundColor = SETTING_COLOR;
        }

        Gui.drawRect(
                x,
                settingY,
                x + width,
                settingY + SETTING_HEIGHT,
                backgroundColor
        );

        mc.fontRendererObj.drawStringWithShadow(
                "Keybind",
                x + 8,
                settingY + centerTextOffset(SETTING_HEIGHT),
                SECONDARY_TEXT_COLOR
        );

        String valueText = listening
                ? "[Press key]"
                : "[" + getKeybindDisplayName(
                module.getKeyBind()
        ) + "]";

        int valueWidth =
                mc.fontRendererObj.getStringWidth(valueText);

        mc.fontRendererObj.drawStringWithShadow(
                valueText,
                x + width - valueWidth - 6,
                settingY + centerTextOffset(SETTING_HEIGHT),
                listening
                        ? TEXT_COLOR
                        : SECONDARY_TEXT_COLOR
        );
    }

    private void drawNumberSetting(
            NumberSetting setting,
            int settingY,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                settingY,
                width,
                SETTING_HEIGHT
        );

        Gui.drawRect(
                x,
                settingY,
                x + width,
                settingY + SETTING_HEIGHT,
                hovered
                        ? SETTING_HOVER_COLOR
                        : SETTING_COLOR
        );

        String valueText = formatNumber(
                setting.getDoubleValue()
        );

        mc.fontRendererObj.drawStringWithShadow(
                setting.getName(),
                x + 8,
                settingY + 3,
                SECONDARY_TEXT_COLOR
        );

        mc.fontRendererObj.drawStringWithShadow(
                valueText,
                x + width
                        - mc.fontRendererObj
                        .getStringWidth(valueText)
                        - 6,
                settingY + 3,
                TEXT_COLOR
        );

        int sliderX = x + 8;
        int sliderY = settingY + SETTING_HEIGHT - 6;
        int sliderWidth = width - 16;
        int sliderHeight = 3;

        Gui.drawRect(
                sliderX,
                sliderY,
                sliderX + sliderWidth,
                sliderY + sliderHeight,
                SLIDER_BACKGROUND_COLOR
        );

        double percentage = getPercentage(setting);

        int filledWidth = (int)Math.round(
                sliderWidth * percentage
        );

        Gui.drawRect(
                sliderX,
                sliderY,
                sliderX + filledWidth,
                sliderY + sliderHeight,
                SLIDER_FILL_COLOR
        );

        /*
         * Small handle at the end of the filled portion.
         */
        int handleX = sliderX + filledWidth;

        Gui.drawRect(
                handleX - 1,
                sliderY - 2,
                handleX + 1,
                sliderY + sliderHeight + 2,
                TEXT_COLOR
        );
    }

    private void drawBooleanSetting(
            BooleanSetting setting,
            int settingY,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                settingY,
                width,
                SETTING_HEIGHT
        );

        Gui.drawRect(
                x,
                settingY,
                x + width,
                settingY + SETTING_HEIGHT,
                hovered
                        ? SETTING_HOVER_COLOR
                        : SETTING_COLOR
        );

        mc.fontRendererObj.drawStringWithShadow(
                setting.getName(),
                x + 8,
                settingY + centerTextOffset(SETTING_HEIGHT),
                SECONDARY_TEXT_COLOR
        );

        String valueText = setting.isEnabled()
                ? "ON"
                : "OFF";

        mc.fontRendererObj.drawStringWithShadow(
                valueText,
                x + width
                        - mc.fontRendererObj
                        .getStringWidth(valueText)
                        - 6,
                settingY + centerTextOffset(SETTING_HEIGHT),
                setting.isEnabled()
                        ? TEXT_COLOR
                        : SECONDARY_TEXT_COLOR
        );
    }

    private void drawUnsupportedSetting(
            Setting<?> setting,
            int settingY,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                settingY,
                width,
                SETTING_HEIGHT
        );

        Gui.drawRect(
                x,
                settingY,
                x + width,
                settingY + SETTING_HEIGHT,
                hovered
                        ? SETTING_HOVER_COLOR
                        : SETTING_COLOR
        );

        mc.fontRendererObj.drawStringWithShadow(
                setting.getName(),
                x + 8,
                settingY + centerTextOffset(SETTING_HEIGHT),
                SECONDARY_TEXT_COLOR
        );

        String valueText = setting.serializeValue();

        mc.fontRendererObj.drawStringWithShadow(
                valueText,
                x + width
                        - mc.fontRendererObj
                        .getStringWidth(valueText)
                        - 6,
                settingY + centerTextOffset(SETTING_HEIGHT),
                TEXT_COLOR
        );
    }

    public boolean mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        /*
         * Category header.
         */
        if (isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                HEADER_HEIGHT
        )) {
            if (mouseButton == 0 || mouseButton == 1) {
                expanded = !expanded;

                draggingSlider = null;
                owner.cancelKeybindCapture();

                return true;
            }
        }

        if (!expanded) {
            return false;
        }

        int currentY = y + HEADER_HEIGHT;

        for (Module module : getModules()) {
            /*
             * Module row.
             */
            if (isInside(
                    mouseX,
                    mouseY,
                    x,
                    currentY,
                    width,
                    MODULE_HEIGHT
            )) {
                if (mouseButton == 0) {
                    owner.cancelKeybindCapture();
                    module.toggle();
                    return true;
                }

                if (mouseButton == 1) {
                    owner.cancelKeybindCapture();

                    setModuleExpanded(
                            module,
                            !isModuleExpanded(module)
                    );

                    return true;
                }
            }

            currentY += MODULE_HEIGHT;

            if (!isModuleExpanded(module)) {
                continue;
            }

            /*
             * Keybind row.
             *
             * Left-click begins listening.
             * Right-click immediately clears the keybind.
             */
            if (isInside(
                    mouseX,
                    mouseY,
                    x,
                    currentY,
                    width,
                    SETTING_HEIGHT
            )) {
                if (mouseButton == 0) {
                    draggingSlider = null;
                    owner.beginKeybindCapture(module);
                    return true;
                }

                if (mouseButton == 1) {
                    module.setKeyBind(Module.UNBOUND_KEY);
                    owner.cancelKeybindCapture();
                    return true;
                }
            }

            currentY += SETTING_HEIGHT;

            /*
             * Normal settings belonging to this module.
             */
            for (Setting<?> setting : module.getSettings()) {
                if (isInside(
                        mouseX,
                        mouseY,
                        x,
                        currentY,
                        width,
                        SETTING_HEIGHT
                )) {
                    owner.cancelKeybindCapture();

                    if (setting instanceof NumberSetting
                            && mouseButton == 0) {

                        beginSliderDrag(
                                (NumberSetting)setting,
                                mouseX
                        );

                        return true;
                    }

                    if (setting instanceof BooleanSetting
                            && mouseButton == 0) {

                        ((BooleanSetting)setting).toggle();
                        return true;
                    }
                }

                currentY += SETTING_HEIGHT;
            }
        }

        return false;
    }

    public void mouseDragged(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        if (mouseButton != 0
                || draggingSlider == null) {
            return;
        }

        updateSliderValue(
                draggingSlider,
                mouseX
        );
    }

    public void mouseReleased(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        if (mouseButton == 0) {
            draggingSlider = null;
        }
    }

    private void beginSliderDrag(
            NumberSetting setting,
            int mouseX
    ) {
        draggingSlider = setting;
        draggingSliderX = x + 8;
        draggingSliderWidth = width - 16;

        updateSliderValue(setting, mouseX);
    }

    private void updateSliderValue(
            NumberSetting setting,
            int mouseX
    ) {
        if (draggingSliderWidth <= 0) {
            return;
        }

        double percentage =
                (double)(mouseX - draggingSliderX)
                        / (double)draggingSliderWidth;

        percentage = clamp(
                percentage,
                0.0D,
                1.0D
        );

        double value =
                setting.getMinimum()
                        + percentage
                        * (
                        setting.getMaximum()
                                - setting.getMinimum()
                );

        /*
         * NumberSetting#setValue invokes normalizeValue(), which should
         * clamp and snap the value to the setting's increment.
         */
        setting.setValue(Double.valueOf(value));
    }

    private double getPercentage(NumberSetting setting) {
        double range =
                setting.getMaximum()
                        - setting.getMinimum();

        if (range <= 0.0D) {
            return 0.0D;
        }

        return clamp(
                (
                        setting.getDoubleValue()
                                - setting.getMinimum()
                ) / range,
                0.0D,
                1.0D
        );
    }

    private String getKeybindDisplayName(int keyCode) {
        if (keyCode == Module.UNBOUND_KEY) {
            return "NONE";
        }

        String displayName =
                GameSettings.getKeyDisplayString(keyCode);

        if (displayName == null
                || displayName.length() == 0) {
            return "#" + keyCode;
        }

        return displayName;
    }

    private String formatNumber(double value) {
        /*
         * Show integers without a decimal. Other values are shown with
         * two decimal places and trailing zeroes removed.
         */
        if (Math.abs(value - Math.round(value))
                < 0.000001D) {

            return Long.toString(Math.round(value));
        }

        String text = String.format("%.2f", value);

        while (text.endsWith("0")) {
            text = text.substring(
                    0,
                    text.length() - 1
            );
        }

        if (text.endsWith(".")) {
            text = text.substring(
                    0,
                    text.length() - 1
            );
        }

        return text;
    }

    private boolean isModuleExpanded(Module module) {
        Boolean value = expandedModules.get(module);

        return value != null
                && value.booleanValue();
    }

    private void setModuleExpanded(
            Module module,
            boolean expanded
    ) {
        expandedModules.put(
                module,
                Boolean.valueOf(expanded)
        );
    }

    private List<Module> getModules() {
        return moduleManager.getByCategory(category);
    }

    private int centerTextOffset(int rowHeight) {
        return (
                rowHeight
                        - mc.fontRendererObj.FONT_HEIGHT
        ) / 2;
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

    private double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
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

        if (!expanded) {
            draggingSlider = null;
            owner.cancelKeybindCapture();
        }
    }
}