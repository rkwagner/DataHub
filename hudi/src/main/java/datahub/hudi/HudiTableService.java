package datahub.hudi;

import datahub.core.contract.Table;
import datahub.core.metadata.SchemaInteroperability;
import datahub.core.services.AbstractDdlGenerator;

import java.util.stream.Collectors;

/**
 * Service responsible for generating Hudi-specific DDL (Data Definition
 * Language) statements,
 * using the abstract schema definition and Hudi properties.
 */
public class HudiTableService extends AbstractDdlGenerator<HudiType> {

    /**
     * Generates a Spark/Hive compatible 'CREATE TABLE IF NOT EXISTS' statement for
     * a Hudi table.
     * This DDL includes the necessary TBLPROPERTIES for Hudi to function correctly.
     * 
     * @param table The complete table definition (Name, Namespace, TypedSchema).
     * @return The DDL string.
     */
    @Override
    public String generateCreateStatement(Table<HudiType> table) {
        HudiProperties hudiProps = (HudiProperties) table.getProperties();

        // 1. Get Core Metadata
        String fullName = table.getNamespace().getValue() + "." + table.getName().getValue();
        final String recordKey = hudiProps.getStringProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY);
        final String precombineKey = hudiProps.getStringProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY);
        final String partitionFields = hudiProps.getStringProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY);
        final String tableType = hudiProps.getStringProperty(HudiType.TableProperties.TABLE_TYPE_KEY);

        // 2. Get Avro Schema
        // This is necessary for engines like Trino/Hive to infer schema, even if they
        // read Parquet files.
        SchemaInteroperability schemaInterop = (SchemaInteroperability) table.getSchema();
        final String avroSchemaString = schemaInterop.toAvroSchemaString()
                .orElseThrow(() -> new IllegalStateException("Failed to generate Avro Schema."));

        // 3. Generate the list of columns for the SQL DDL
        String columnsDefinition = table.getSchema().getFields().stream()
                // Format: column_name data_type COMMENT '...'
                .map(field -> String.format("  %s %s COMMENT 'Canonical field: %s'",
                        field.getName(),
                        mapDataTypeToSql(field.getDataType()),
                        field.getName()))
                .collect(Collectors.joining(",\n"));

        // 4. Build the final DDL statement
        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("CREATE TABLE IF NOT EXISTS %s (\n%s\n)\n", fullName, columnsDefinition));

        // Add PARTITIONED BY clause
        if (partitionFields != null && !partitionFields.isEmpty()) {
            // Assuming partitionFields is comma-separated for simple DDL
            ddl.append(String.format("PARTITIONED BY (%s)\n", partitionFields.replace(",", ", ")));
        }

        // Add STORED AS Hudi format
        ddl.append("STORED AS HUDI\n");

        // Add TBLPROPERTIES (Hudi configuration)
        ddl.append("TBLPROPERTIES (\n");
        ddl.append(String.format("  '%s'='%s',\n", HudiType.TableProperties.TABLE_TYPE_KEY, tableType));
        ddl.append(String.format("  '%s'='%s',\n", HudiType.TableProperties.RECORD_KEY_FIELD_KEY, recordKey));
        ddl.append(String.format("  '%s'='%s',\n", HudiType.TableProperties.PRECOMBINE_FIELD_KEY, precombineKey));
        ddl.append(String.format("  '%s'='%s',\n", HudiType.TableProperties.PARTITION_FIELDS_KEY, partitionFields));
        // Include the Avro Schema string in properties (mandatory for complex Hudi
        // setups)
        ddl.append(String.format("  'schema.external.avro.raw'='%s',\n",
                avroSchemaString.replace("'", "\\'").replace("\n", " ")));

        // Use centralized comment
        ddl.append(String.format("  'comment'='%s'\n", getGeneratedByComment()));
        ddl.append(")\n");

        return ddl.toString();
    }

    /**
     * Static convenience method for backward compatibility.
     * Delegates to the instance method.
     * 
     * @param table The table definition.
     * @return The DDL string.
     */
    public static String generateCreateStatementStatic(Table<HudiType> table) {
        return new HudiTableService().generateCreateStatement(table);
    }

    /**
     * Hudi/Spark/Hive often prefer generic TIMESTAMP and rely on metadata for
     * precision.
     */
    @Override
    protected String mapDataTypeToSql(datahub.core.metadata.DataType dataType) {
        return switch (dataType) {
            case TIMESTAMP_MICROS, TIMESTAMP_MILLIS -> "TIMESTAMP";
            default -> super.mapDataTypeToSql(dataType);
        };
    }
}