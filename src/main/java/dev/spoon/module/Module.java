package dev.spoon.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.spoon.event.VelocityEvent;
import dev.spoon.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public abstract class Module {

    public static final int UNBOUND_KEY = -1;

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final ModuleCategory category;

    private final List<Setting<?>> settings = new ArrayList<>();

    private boolean enabled;
    private int keyBind;

    protected Module(
            String name,
            String description,
            ModuleCategory category,
            int keyBind
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = keyBind;
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }

    public void onRender2D(float partialTicks) {
    }

    public void onRender3D(float partialTicks) {
    }

    protected final <T extends Setting<?>> T registerSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public final Setting<?> getSettingByName(String name) {
        if (name == null) {
            return null;
        }

        for (Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public void onVelocity(VelocityEvent event) {
        // pass?? lol
    }

    public void onAttackEntity(
            Entity target,
            boolean wasSprinting
    ) {
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final ModuleCategory getCategory() {
        return category;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final int getKeyBind() {
        return keyBind;
    }

    public final void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public final List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }
}