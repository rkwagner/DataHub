package datahub.core.metadata;

/**
 * Concrete implementation of DomainField using records for simplicity and
 * immutability.
 */
public class FieldImpl implements Field {
    private final String name;
    private final DataType dataType;
    private final boolean isPrimaryKey;
    private final FieldConstraints constraints;

    public FieldImpl(String name, DataType dataType, boolean isPrimaryKey, FieldConstraints constraints) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.constraints = constraints;
    }

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
