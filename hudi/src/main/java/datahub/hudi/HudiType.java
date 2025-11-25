package datahub.hudi;

import org.apache.hudi.common.model.HoodieTableType;
import org.apache.hudi.common.model.WriteOperationType;
import org.apache.hudi.common.table.HoodieTableConfig;

import datahub.core.contract.DBType;

/**
 * Hudi-specific database type definition using actual Hudi configuration
 * properties.
 */
public interface HudiType extends DBType {

    /**
     * Core table properties from Hudi's configuration.
     */
    final class TableProperties {
        private TableProperties() {
            throw new AssertionError("Cannot instantiate TableProperties");
        }

        // Table type: COPY_ON_WRITE or MERGE_ON_READ
        public static final String TABLE_TYPE_KEY = HoodieTableConfig.TYPE.key();
        public static final String COPY_ON_WRITE = HoodieTableType.COPY_ON_WRITE.name();
        public static final String MERGE_ON_READ = HoodieTableType.MERGE_ON_READ.name();

        // Record key field
        public static final String RECORD_KEY_FIELD_KEY = HoodieTableConfig.RECORDKEY_FIELDS.key();

        // Partition path field
        public static final String PARTITION_FIELDS_KEY = HoodieTableConfig.PARTITION_FIELDS.key();

        // Precombine field
        public static final String PRECOMBINE_FIELD_KEY = HoodieTableConfig.PRECOMBINE_FIELD.key();
    }

    /**
     * Write operation properties from Hudi's WriteOperationType.
     */
    final class WriteOperations {
        private WriteOperations() {
            throw new AssertionError("Cannot instantiate WriteOperations");
        }

        public static final String UPSERT = WriteOperationType.UPSERT.value();
        public static final String INSERT = WriteOperationType.INSERT.value();
        public static final String BULK_INSERT = WriteOperationType.BULK_INSERT.value();
        public static final String DELETE = WriteOperationType.DELETE.value();
        public static final String INSERT_OVERWRITE = WriteOperationType.INSERT_OVERWRITE.value();
        public static final String INSERT_OVERWRITE_TABLE = WriteOperationType.INSERT_OVERWRITE_TABLE.value();
    }

    /**
     * Read query type properties.
     */
    final class ReadQueryTypes {
        private ReadQueryTypes() {
            throw new AssertionError("Cannot instantiate ReadQueryTypes");
        }

        public static final String SNAPSHOT = "snapshot";
        public static final String INCREMENTAL = "incremental";
        public static final String READ_OPTIMIZED = "read_optimized";
    }
}
