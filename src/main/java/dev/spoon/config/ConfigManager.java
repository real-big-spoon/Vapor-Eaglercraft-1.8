package dev.spoon.config;

import java.util.HashMap;
import java.util.Map;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleManager;
import dev.spoon.setting.Setting;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;

public final class ConfigManager {

    private static final String CONFIG_DIRECTORY = "spoon/configs";
    private static final int CONFIG_VERSION = 1;

    private final ModuleManager moduleManager;

    public ConfigManager(ModuleManager moduleManager) {
        if (moduleManager == null) {
            throw new IllegalArgumentException("ModuleManager cannot be null");
        }

        this.moduleManager = moduleManager;
    }

    /**
     * Converts the current module configuration into text.
     *
     * This method is independent from storage so the same text can later be
     * exported through a browser download or imported through a file chooser.
     */
    public String serializeCurrentConfig() {
        StringBuilder builder = new StringBuilder();

        builder.append("# Spoon client configuration\n");
        builder.append("version|")
                .append(CONFIG_VERSION)
                .append('\n');

        for (Module module : moduleManager.getModules()) {
            builder.append("module|")
                    .append(module.getName())
                    .append("|enabled|")
                    .append(module.isEnabled())
                    .append('\n');

            builder.append("module|")
                    .append(module.getName())
                    .append("|keybind|")
                    .append(module.getKeyBind())
                    .append('\n');

            for (Setting<?> setting : module.getSettings()) {
                builder.append("setting|")
                        .append(module.getName())
                        .append('|')
                        .append(setting.getName())
                        .append('|')
                        .append(setting.serializeValue())
                        .append('\n');
            }
        }

        return builder.toString();
    }

    /**
     * Applies configuration text to the currently registered modules.
     */
    public boolean applyConfigText(String configText) {
        if (configText == null) {
            return false;
        }

        /*
         * Enabled states are delayed until after settings are loaded.
         *
         * Otherwise onEnable() could run before the module receives its saved
         * settings.
         */
        Map<Module, Boolean> pendingEnabledStates = new HashMap<>();

        String normalizedText = configText.replace("\r", "");
        String[] lines = normalizedText.split("\n");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            /*
             * Limit 4 means a setting value may contain additional pipe
             * characters without breaking the parser.
             */
            String[] parts = line.split("\\|", 4);

            if (parts.length < 2) {
                continue;
            }

            String entryType = parts[0];

            if ("version".equalsIgnoreCase(entryType)) {
                // Version migration can be handled here later.
                continue;
            }

            if ("module".equalsIgnoreCase(entryType)) {
                applyModuleEntry(parts, pendingEnabledStates);
                continue;
            }

            if ("setting".equalsIgnoreCase(entryType)) {
                applySettingEntry(parts);
            }
        }

        /*
         * Enable and disable modules only after keybinds and settings have
         * finished loading.
         */
        for (Map.Entry<Module, Boolean> entry
                : pendingEnabledStates.entrySet()) {

            entry.getKey().setEnabled(entry.getValue().booleanValue());
        }

        return true;
    }

    private void applyModuleEntry(
            String[] parts,
            Map<Module, Boolean> pendingEnabledStates
    ) {
        if (parts.length != 4) {
            return;
        }

        String moduleName = parts[1];
        String propertyName = parts[2];
        String propertyValue = parts[3];

        Module module = moduleManager.getByName(moduleName);

        if (module == null) {
            /*
             * This permits loading older configs after a module was removed.
             */
            return;
        }

        if ("enabled".equalsIgnoreCase(propertyName)) {
            if ("true".equalsIgnoreCase(propertyValue)) {
                pendingEnabledStates.put(module, Boolean.TRUE);
            } else if ("false".equalsIgnoreCase(propertyValue)) {
                pendingEnabledStates.put(module, Boolean.FALSE);
            }

            return;
        }

        if ("keybind".equalsIgnoreCase(propertyName)) {
            try {
                module.setKeyBind(Integer.parseInt(propertyValue));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void applySettingEntry(String[] parts) {
        if (parts.length != 4) {
            return;
        }

        String moduleName = parts[1];
        String settingName = parts[2];
        String serializedValue = parts[3];

        Module module = moduleManager.getByName(moduleName);

        if (module == null) {
            return;
        }

        Setting<?> setting = module.getSettingByName(settingName);

        if (setting == null) {
            /*
             * This permits loading older configs after settings were removed.
             */
            return;
        }

        setting.deserializeValue(serializedValue);
    }

    public boolean save(String configName) {
        VFile2 configFile = getConfigFile(configName);

        if (configFile == null) {
            return false;
        }

        try {
            configFile.setAllChars(serializeCurrentConfig());
            return true;
        } catch (Throwable throwable) {
            System.err.println(
                    "Failed to save Spoon config: " + configName
            );
            throwable.printStackTrace();
            return false;
        }
    }

    public boolean load(String configName) {
        VFile2 configFile = getConfigFile(configName);

        if (configFile == null || !configFile.exists()) {
            return false;
        }

        try {
            String configText = configFile.getAllChars();

            if (configText == null) {
                return false;
            }

            return applyConfigText(configText);
        } catch (Throwable throwable) {
            System.err.println(
                    "Failed to load Spoon config: " + configName
            );
            throwable.printStackTrace();
            return false;
        }
    }

    public boolean exists(String configName) {
        VFile2 configFile = getConfigFile(configName);
        return configFile != null && configFile.exists();
    }

    public boolean delete(String configName) {
        VFile2 configFile = getConfigFile(configName);

        if (configFile == null || !configFile.exists()) {
            return false;
        }

        return configFile.delete();
    }

    private VFile2 getConfigFile(String configName) {
        String safeName = sanitizeConfigName(configName);

        if (safeName.isEmpty()) {
            return null;
        }

        return new VFile2(
                CONFIG_DIRECTORY,
                safeName + ".txt"
        );
    }

    private String sanitizeConfigName(String configName) {
        if (configName == null) {
            return "";
        }

        String trimmedName = configName.trim();
        StringBuilder safeName = new StringBuilder();

        for (int i = 0; i < trimmedName.length(); ++i) {
            char character = trimmedName.charAt(i);

            boolean allowed =
                    character >= 'a' && character <= 'z'
                            || character >= 'A' && character <= 'Z'
                            || character >= '0' && character <= '9'
                            || character == '-'
                            || character == '_';

            safeName.append(allowed ? character : '_');
        }

        return safeName.toString();
    }
}