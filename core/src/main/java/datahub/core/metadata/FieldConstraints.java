package datahub.core.metadata;

import java.util.Optional;

/**
 * Defines the logical constraints and default values for a DomainField
 * within a specific table context.
 */
public interface FieldConstraints {
    /**
     * Checks if the field is nullable.
     *
     * @return True if the column is nullable in this table.
     */
    boolean isNullable();

    /**
     * Gets the default value of the field.
     *
     * @return An optional default value to use for missing records (e.g., -1 for a
     *         BIGINT ID).
     */
    Optional<Object> getDefaultValue();

    /**
     * Gets the default value or null if not present.
     *
     * @return The default value, or null if not present.
     */
    default Object getDefaultValueOrNull() {
        return getDefaultValue().orElse(null);
    }
}