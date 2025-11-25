package datahub.core.schema;

import org.junit.jupiter.api.Test;

import datahub.core.contract.DBType;
import datahub.core.contract.Properties;
import datahub.core.contract.Table;
import datahub.core.contract.TableName;
import datahub.core.contract.TableNamespace;
import datahub.core.metadata.TypedSchema;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    // Concrete DBType for testing
    interface TestDBType extends DBType {
    }

    @Test
    void testTableContract() {
        TableName expectedName = () -> datahub.core.TestConstants.TEST_TABLE_NAME;
        TableNamespace expectedNamespace = () -> datahub.core.TestConstants.TEST_NAMESPACE;
        Properties<TestDBType> expectedProperties = new Properties<>();

        Table<TestDBType> table = new Table<>() {
            @Override
            public TableName getName() {
                return expectedName;
            }

            @Override
            public TableNamespace getNamespace() {
                return expectedNamespace;
            }

            @Override
            public Properties<TestDBType> getProperties() {
                return expectedProperties;
            }

            @Override
            public TypedSchema getSchema() {
                return null;
            }
        };

        assertEquals(expectedName, table.getName(), "Table name should match");
        assertEquals(expectedNamespace, table.getNamespace(), "Namespace should match");
        assertEquals(expectedProperties, table.getProperties(), "Properties should match");
    }
}
