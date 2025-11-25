package datahub.core.metadata;

public interface Field {
    /**
     * Gets the name of the field.
     *
     * @return The canonical, universally unique name of this field (e.g.,
     *         "user_id").
     */
    String getName();

    /**
     * Gets the data type of the field.
     *
     * @return The fundamental data type (e.g., STRING, BIGINT, TIMESTAMP).
     */
    DataType getDataType();

    /**
     * Checks if the field is a primary key.
     *
     * @return True if this field is intended to be the unique identifier (primary
     *         key).
     */
    boolean isPrimaryKey();

    /**
     * Gets the constraints of the field.
     *
     * @return The specific configuration for this field within a table (e.g.,
     *         nullable, default value).
     *         This allows a single DomainField to be used slightly differently
     *         (e.g., nullable or not)
     *         based on the table's specific requirements.
     */
    FieldConstraints getConstraints();
}
