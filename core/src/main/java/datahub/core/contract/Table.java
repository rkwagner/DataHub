package datahub.core.contract;

import datahub.core.metadata.TypedSchema;

/**
 * Represents a data table definition within the system.
 * This interface serves as the foundational building block for schema
 * management.
 */
// TODO: Refactor into using a builder, never ever touching table directly.
public interface Table<T extends DBType> {
    /**
     * Gets the name of the table.
     *
     * @return The unique name of the table.
     */
    TableName getName();

    /**
     * Gets the namespace of the table.
     *
     * @return The namespace (schema, dataset, collection, etc.) this table belongs
     *         to.
     */
    TableNamespace getNamespace();

    /**
     * Gets the properties of the table.
     *
     * @return Strongly-typed properties specific to the DBType.
     */
    Properties<T> getProperties();

    /**
     * Gets the schema of the table.
     *
     * @return The structural schema of the table, composed of concrete Fields.
     */
    TypedSchema getSchema();
}
