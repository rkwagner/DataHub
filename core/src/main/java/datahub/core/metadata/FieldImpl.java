package datahub.core.metadata;

/**
 * Concrete implementation of DomainField using records for simplicity and
 * immutability.
 */
public record FieldImpl(
        String name,
        DataType dataType,
        boolean isPrimaryKey,
        FieldConstraints constraints) implements Field {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    @Override
    public FieldConstraints getConstraints() {
        return constraints;
    }
}
