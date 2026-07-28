package dev.spoon.setting;

public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean isEnabled() {
        return getValue().booleanValue();
    }

    public void toggle() {
        setValue(!isEnabled());
    }

    @Override
    protected Boolean parseValue(String serializedValue) {
        if ("true".equalsIgnoreCase(serializedValue)) {
            return Boolean.TRUE;
        }

        if ("false".equalsIgnoreCase(serializedValue)) {
            return Boolean.FALSE;
        }

        throw new IllegalArgumentException(
                "Invalid boolean: " + serializedValue
        );
    }
}