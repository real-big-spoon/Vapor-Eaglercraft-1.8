package dev.spoon.setting;

import java.util.Objects;

public abstract class Setting<T> {

    private final String name;
    private final T defaultValue;

    private T value;

    protected Setting(String name, T defaultValue) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Setting name cannot be empty");
        }

        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public final String getName() {
        return name;
    }

    public final T getValue() {
        return value;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public final void setValue(T value) {
        T normalizedValue = normalizeValue(value);

        if (Objects.equals(this.value, normalizedValue)) {
            return;
        }

        T previousValue = this.value;
        this.value = normalizedValue;

        onValueChanged(previousValue, normalizedValue);
    }

    public final void reset() {
        setValue(defaultValue);
    }

    public final boolean isDefaultValue() {
        return Objects.equals(value, defaultValue);
    }

    /**
     * Converts the current value into config-file text.
     */
    public final String serializeValue() {
        return valueToString(value);
    }

    /**
     * Parses config-file text and applies the resulting value.
     *
     * @return true if parsing succeeded
     */
    public final boolean deserializeValue(String serializedValue) {
        try {
            setValue(parseValue(serializedValue));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    protected T normalizeValue(T value) {
        return value;
    }

    protected String valueToString(T value) {
        return String.valueOf(value);
    }

    protected abstract T parseValue(String serializedValue);

    protected void onValueChanged(T previousValue, T newValue) {
    }
}