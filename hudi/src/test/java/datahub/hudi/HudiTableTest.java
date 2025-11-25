package datahub.hudi;

import datahub.core.contract.Properties;
import datahub.core.contract.Table;
import datahub.core.contract.TableName;
import datahub.core.contract.TableNamespace;
import datahub.core.metadata.TypedSchema;

import static datahub.core.TestConstants.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudiTableTest {

    @Test
    void testHudiTableProperties() {
        System.out.println("testHudiTableProperties Executing basic test");
        // Accessing constants directly without full qualification now (due to static
        // import)
        final TableName name = () -> HUDI_TABLE_NAME;
        final TableNamespace namespace = () -> DATA_LAKE_NAMESPACE;
        HudiProperties properties = new HudiProperties();

        // Set table type using Hudi's actual configuration key and value
        properties.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY,
                HudiType.TableProperties.COPY_ON_WRITE);

        Table<HudiType> table = new Table<>() {
            @Override
            public TableName getName() {
                return name;
            }

            @Override
            public TableNamespace getNamespace() {
                return namespace;
            }

            @Override
            public Properties<HudiType> getProperties() {
                return properties;
            }

            @Override
            public TypedSchema getSchema() {
                return null;
            }
        };

        assertEquals(HudiType.TableProperties.COPY_ON_WRITE,
                ((HudiProperties) table.getProperties())
                        .getProperty(HudiType.TableProperties.TABLE_TYPE_KEY));
    }

    @Test
    void testHudiWriteOperations() {
        System.out.println("testHudiWriteOperations Attempting basic write operation parameter tests");
        HudiProperties properties = new HudiProperties();
        int splitSize = 120000;

        // Set write operation using Hudi's actual WriteOperationType values
        properties.setProperty("hoodie.datasource.write.operation", HudiType.WriteOperations.UPSERT);
        properties.setProperty("hoodie.datasource.write.insert.split.size", splitSize);

        // Verify write properties
        assertEquals(HudiType.WriteOperations.UPSERT,
                properties.getProperty("hoodie.datasource.write.operation"));
        assertEquals(splitSize,
                properties.getProperty("hoodie.datasource.write.insert.split.size"));
    }

    @Test
    void testHudiReadQueryTypes() {
        HudiProperties properties = new HudiProperties();

        // Set read query type using Hudi's actual query type values
        properties.setProperty("hoodie.datasource.query.type", HudiType.ReadQueryTypes.SNAPSHOT);

        properties.setProperty("hoodie.datasource.read.begin.instanttime",
                getStartTimeString());

        // Verify read properties
        assertEquals(HudiType.ReadQueryTypes.SNAPSHOT,
                properties.getProperty("hoodie.datasource.query.type"));
        assertEquals(getStartTimeString(),
                properties.getProperty("hoodie.datasource.read.begin.instanttime"));
    }

    @Test
    void testHudiTableConfiguration() {
        HudiProperties properties = new HudiProperties();

        // Set comprehensive table configuration using Hudi's actual config keys
        properties.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY,
                HudiType.TableProperties.MERGE_ON_READ);
        properties.setProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY, "user_id");
        properties.setProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY, "date");
        properties.setProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY, "timestamp");

        // Verify all properties
        assertEquals(HudiType.TableProperties.MERGE_ON_READ,
                properties.getStringProperty(HudiType.TableProperties.TABLE_TYPE_KEY));
        assertEquals("user_id",
                properties.getStringProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY));
        assertEquals("date",
                properties.getStringProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY));
        assertEquals("timestamp",
                properties.getStringProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY));
    }

    @Test
    void testAllWriteOperationTypes() {
        HudiProperties properties = new HudiProperties();

        // Test all Hudi write operation types
        String[] operations = {
            HudiType.WriteOperations.UPSERT,
            HudiType.WriteOperations.INSERT,
            HudiType.WriteOperations.BULK_INSERT,
            HudiType.WriteOperations.DELETE,
            HudiType.WriteOperations.INSERT_OVERWRITE,
            HudiType.WriteOperations.INSERT_OVERWRITE_TABLE
        };

        for (String operation : operations) {
            properties.setProperty("hoodie.datasource.write.operation", operation);
            assertEquals(operation,
                    properties.getProperty("hoodie.datasource.write.operation"));
        }
    }

    @Test
    void testMissingMandatoryProperties() {
        final TableName name = () -> HUDI_TABLE_NAME;
        final TableNamespace namespace = () -> DATA_LAKE_NAMESPACE;
        HudiProperties properties = new HudiProperties();
        // Only setting one property, missing others like RECORD_KEY_FIELD_KEY
        properties.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY,
                HudiType.TableProperties.COPY_ON_WRITE);

        // Create a dummy schema
        final TypedSchema schema = () -> java.util.Collections.emptyList();

        Exception exception = assertThrows(datahub.core.exceptions.SchemaValidationException.class, () -> {
            new HudiTable(name, namespace, schema, properties);
        });

        assertTrue(exception.getMessage().contains("Mandatory property"));
    }

    @Test
    void testInvalidFieldInProperties() {
        final TableName name = () -> HUDI_TABLE_NAME;
        final TableNamespace namespace = () -> DATA_LAKE_NAMESPACE;
        HudiProperties properties = new HudiProperties();

        properties.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY,
                HudiType.TableProperties.COPY_ON_WRITE);
        properties.setProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY, "id");
        properties.setProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY, "ts");
        properties.setProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY, "partition_col");

        // Schema only has "id" and "ts", but missing "partition_col"
        final TypedSchema schema = () -> java.util.List.of(
                new datahub.core.metadata.FieldImpl("id", datahub.core.metadata.DataType.STRING, true,
                        null),
                new datahub.core.metadata.FieldImpl("ts", datahub.core.metadata.DataType.BIGINT, false,
                        null));

        Exception exception = assertThrows(datahub.core.exceptions.SchemaValidationException.class, () -> {
            new HudiTable(name, namespace, schema, properties);
        });

        assertTrue(exception.getMessage().contains("is not present in the canonical table schema"));
    }

    @Test
    void testValidProperties() {
        final TableName name = () -> HUDI_TABLE_NAME;
        final TableNamespace namespace = () -> DATA_LAKE_NAMESPACE;
        HudiProperties properties = new HudiProperties();

        properties.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY,
                HudiType.TableProperties.COPY_ON_WRITE);
        properties.setProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY, "id");
        properties.setProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY, "ts");
        properties.setProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY, "partition_col");

        // Schema has all required fields
        final TypedSchema schema = () -> java.util.List.of(
                new datahub.core.metadata.FieldImpl("id", datahub.core.metadata.DataType.STRING, true,
                        null),
                new datahub.core.metadata.FieldImpl("ts", datahub.core.metadata.DataType.BIGINT, false,
                        null),
                new datahub.core.metadata.FieldImpl("partition_col",
                        datahub.core.metadata.DataType.STRING, false, null));

        assertDoesNotThrow(() -> {
            new HudiTable(name, namespace, schema, properties);
        });
    }
}
