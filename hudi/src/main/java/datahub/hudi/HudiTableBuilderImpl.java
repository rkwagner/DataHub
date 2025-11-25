package datahub.hudi;

import datahub.core.contract.TableBuilder;
import datahub.core.contract.Table;
import datahub.core.contract.TableName;
import datahub.core.contract.TableNamespace;
import datahub.core.contract.Properties;
import datahub.core.metadata.TypedSchema;
import datahub.core.exceptions.SchemaValidationException;

/**
 * Implementation of the TableBuilder pattern for Hudi tables.
 * This class collects the required components and constructs the immutable
 * HudiTable.
 */
public class HudiTableBuilderImpl implements TableBuilder<HudiType> {

    // Required components
    private TableName name;
    private TableNamespace namespace;
    private TypedSchema schema;
    private Properties<HudiType> properties;

    public static HudiTableBuilderImpl builder() {
        return new HudiTableBuilderImpl();
    }

    @Override
    public TableBuilder<HudiType> withName(TableName name) {
        this.name = name;
        return this;
    }

    @Override
    public TableBuilder<HudiType> withNamespace(TableNamespace namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public TableBuilder<HudiType> withSchema(TypedSchema schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public TableBuilder<HudiType> withProperties(Properties<HudiType> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Builds the final immutable HudiTable instance.
     * The HudiTable constructor internally runs the key validation
     * (validateConstruction).
     * 
     * @throws SchemaValidationException if the configured properties do not match
     *                                   the schema.
     */
    @Override
    public Table<HudiType> build() throws SchemaValidationException {
        // The constructor call executes the required validation hook
        // (validateConstruction)
        // defined in HudiTable before the instance is fully constructed.
        return new HudiTable(name, namespace, schema, properties);
    }
}