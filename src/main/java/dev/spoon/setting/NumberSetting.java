package dev.spoon.setting;

public final class NumberSetting extends Setting<Double> {

    private final double minimum;
    private final double maximum;
    private final double increment;

    public NumberSetting(
            String name,
            double defaultValue,
            double minimum,
            double maximum,
            double increment
    ) {
        super(name, validateDefault(defaultValue, minimum, maximum));

        if (maximum < minimum) {
            throw new IllegalArgumentException(
                    "Maximum cannot be lower than minimum"
            );
        }

        if (increment <= 0.0D) {
            throw new IllegalArgumentException(
                    "Increment must be greater than zero"
            );
        }

        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    private static double validateDefault(
            double value,
            double minimum,
            double maximum
    ) {
        if (maximum < minimum) {
            throw new IllegalArgumentException(
                    "Maximum cannot be lower than minimum"
            );
        }

        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    "Default value is outside the allowed range"
            );
        }

        return value;
    }

    @Override
    protected Double normalizeValue(Double value) {
        if (value == null) {
            return getDefaultValue();
        }

        double clamped = Math.max(
                minimum,
                Math.min(maximum, value.doubleValue())
        );

        double snapped = minimum
                + Math.round((clamped - minimum) / increment)
                * increment;

        return Math.max(minimum, Math.min(maximum, snapped));
    }

    @Override
    protected Double parseValue(String serializedValue) {
        return Double.valueOf(serializedValue);
    }

    public double getDoubleValue() {
        return getValue().doubleValue();
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getIncrement() {
        return increment;
    }
}