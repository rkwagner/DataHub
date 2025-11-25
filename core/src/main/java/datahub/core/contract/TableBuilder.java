package datahub.core.contract;

import datahub.core.metadata.TypedSchema;
import datahub.core.exceptions.SchemaValidationException;

/**
 * Defines the contract for building a valid, immutable Table object.
 * Builders enforce the rule that a Table cannot be constructed without the
 * minimum
 * required schema and properties, and ensures internal consistency
 * (validation).
 */
public interface TableBuilder<T extends DBType> {

    TableBuilder<T> withName(TableName name);

    TableBuilder<T> withNamespace(TableNamespace namespace);

    TableBuilder<T> withSchema(TypedSchema schema);

    TableBuilder<T> withProperties(Properties<T> properties);

    /**
     * Builds the final immutable Table instance.
     * This method MUST internally call a validation routine to check for
     * schema/property mismatches.
     * 
     * @return The immutable Table instance.
     * @throws SchemaValidationException if the configured properties do not match
     *                                   the schema.
     */
    Table<T> build() throws SchemaValidationException;
}