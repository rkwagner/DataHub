package datahub.core.contract;

import datahub.core.metadata.TypedSchema;
import datahub.core.exceptions.SchemaValidationException;

/**
 * Abstract base class for immutable Table implementations.
 * It ensures the table is constructed with all required components.
 * Subclasses will define the specific validation logic.
 */
public abstract class AbstractTable<T extends DBType> implements Table<T> {

    protected final TableName name;
    protected final TableNamespace namespace;
    protected final TypedSchema schema;
    protected final Properties<T> properties;

    protected AbstractTable(
            TableName name,
            TableNamespace namespace,
            TypedSchema schema,
            Properties<T> properties)
            throws SchemaValidationException {

        if (name == null || namespace == null || schema == null || properties == null) {
            throw new IllegalArgumentException("Table construction requires Name, Namespace, Schema, and Properties.");
        }

        this.name = name;
        this.namespace = namespace;
        this.schema = schema;
        this.properties = properties;

        validateConstruction(this);
    }

    // --- Abstract Validation Hook ---

    /**
     * Must be implemented by concrete subclasses (e.g., HudiTable) to run
     * type-specific
     * validation against the schema and properties.
     */
    protected abstract void validateConstruction(Table<T> finalTable) throws SchemaValidationException;

    // --- Accessors ---

    @Override
    public TableName getName() {
        return name;
    }

    @Override
    public TableNamespace getNamespace() {
        return namespace;
    }

    @Override
    public TypedSchema getSchema() {
        return schema;
    }

    @Override
    public Properties<T> getProperties() {
        return properties;
    }
}