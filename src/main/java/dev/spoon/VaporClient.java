package dev.spoon;

import net.lax1dude.eaglercraft.v1_8.Keyboard;

import dev.spoon.gui.clickgui.ClickGuiScreen;
import dev.spoon.module.ModuleManager;
import net.minecraft.client.Minecraft;
import dev.spoon.config.ConfigManager;

import dev.spoon.module.impl.hud.ModData;

public class VaporClient {

    public static final String NAME = "Vapor";
    public static final String VERSION = "0.1.0";

    private static final int KEY_RIGHT_SHIFT = 54;

    private static final VaporClient INSTANCE = new VaporClient();
    private final ModuleManager moduleManager = new ModuleManager();
    private final ConfigManager configManager =
            new ConfigManager(moduleManager);

    private boolean initialized;

    private VaporClient() {

    }

    public static VaporClient getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        // reg
        moduleManager.register(new ModData());
        initialized = true;

        /*
         * Load only after every module has been registered.
         */
        if (!configManager.load("default")) {
            configManager.save("default");
        }
    }


    public void onKeyPressed(int keyCode) {
        if (!initialized) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (keyCode == KEY_RIGHT_SHIFT) {
            if (mc.currentScreen instanceof ClickGuiScreen) {
                mc.displayGuiScreen(null);
            } else if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ClickGuiScreen());
            }

            return;
        }

        if (mc.currentScreen == null) {
            boolean changed = moduleManager.onKeyPressed(keyCode);

            if (changed) {
                configManager.save("default");
            }
        }
    }

    public void onTick() {
        if (!initialized) {
            return;
        }
        moduleManager.onTick();
    }

    public void onRender2D(float partialTicks) {
        if (!initialized) {
            return;
        }
        moduleManager.onRender2D(partialTicks);
    }

    public void onRender3D(float partialTicks) {
        moduleManager.onRender3D(partialTicks);
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}