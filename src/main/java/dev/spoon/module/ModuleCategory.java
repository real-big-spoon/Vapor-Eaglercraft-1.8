package dev.spoon.module;

public enum ModuleCategory {

    HUD("HUD"),
    RENDER("Render"),
    PLAYER("Player"),
    COMBAT("Combat"),
    WORLD("World"),
    MOVEMENT("Movement");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}