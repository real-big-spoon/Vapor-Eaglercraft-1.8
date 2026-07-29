package dev.spoon.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.spoon.event.VelocityEvent;
import net.minecraft.entity.Entity;

public final class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void register(Module module) {
        if (module == null || modules.contains(module)) {
            return;
        }

        modules.add(module);
    }

    public boolean onKeyPressed(int keyCode) {
        if (keyCode == Module.UNBOUND_KEY) {
            return false;
        }

        boolean changed = false;

        for (Module module : modules) {
            if (
                    module.getKeyBind() != Module.UNBOUND_KEY
                            && module.getKeyBind() == keyCode
            ) {
                module.toggle();
                changed = true;
            }
        }

        return changed;
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onRender2D(float partialTicks) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender2D(partialTicks);
            }
        }
    }

    public void onRender3D(float partialTicks) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender3D(partialTicks);
            }
        }
    }

    public void onVelocity(VelocityEvent event) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onVelocity(event);
            }
        }
    }

    public void onAttackEntity(
            Entity target,
            boolean wasSprinting
    ) {
        if (target == null) {
            return;
        }

        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onAttackEntity(
                        target,
                        wasSprinting
                );
            }
        }
    }

    public Module getByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }

    public <T extends Module> T getByClass(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }

        return null;
    }

    public List<Module> getByCategory(ModuleCategory category) {
        List<Module> result = new ArrayList<>();

        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }
}