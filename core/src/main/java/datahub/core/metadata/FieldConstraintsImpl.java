package datahub.core.metadata;

import java.util.Optional;

/**
 * Concrete implementation of FieldConstraints to define nullability and default
 * values.
 */
public record FieldConstraintsImpl(
        boolean isNullable,
        Optional<Object> defaultValue) implements FieldConstraints {

    public FieldConstraintsImpl {
        // Ensure that if a field is non-nullable, it has a default value (best
        // practice)
        if (!isNullable && defaultValue.isEmpty()) {
            throw new IllegalArgumentException("Non-nullable fields must specify a default value.");
        }
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return defaultValue;
    }
}
