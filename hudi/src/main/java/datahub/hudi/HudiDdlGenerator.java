package datahub.hudi;

import datahub.core.contract.Table;
import datahub.core.metadata.Field;
import datahub.core.services.AbstractDdlGenerator;
import java.util.stream.Collectors;

public class HudiDdlGenerator extends AbstractDdlGenerator<HudiType> {

        @Override
        public String generateCreateStatement(Table<HudiType> table) {
                StringBuilder ddl = new StringBuilder();
                ddl.append("-- ").append(getGeneratedByComment()).append("\n");
                ddl.append("CREATE TABLE IF NOT EXISTS ").append(table.getName().getValue()).append(" (\n");

                String columns = table.getSchema().getFields().stream()
                                .map(field -> "  " + field.getName() + " " + mapDataTypeToSql(field.getDataType()))
                                .collect(Collectors.joining(",\n"));
                ddl.append(columns);
                ddl.append("\n) USING hudi\n");

                HudiProperties props = (HudiProperties) table.getProperties();

                // Handle Options
                ddl.append("OPTIONS (\n");
                ddl.append("  type = '").append(props.getStringProperty(HudiType.TableProperties.TABLE_TYPE_KEY))
                                .append("',\n");
                ddl.append("  primaryKey = '")
                                .append(props.getStringProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY))
                                .append("',\n");
                ddl.append("  preCombineField = '")
                                .append(props.getStringProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY))
                                .append("'\n");
                ddl.append(")\n");

                // Handle Partitioning
                String partitionField = props.getStringProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY);
                if (partitionField != null && !partitionField.isEmpty()) {
                        ddl.append("PARTITIONED BY (").append(partitionField).append(")\n");
                }

                // Location is typically handled by the runtime or external config, but we can
                // append it if needed.
                // For now, we rely on the Spark session's default or explicit path handling in
                // App.java.

                return ddl.toString();
        }

        @Override
        protected String mapDataTypeToSql(datahub.core.metadata.DataType dataType) {
                switch (dataType) {
                        case TIMESTAMP_MICROS:
                                return "BIGINT"; // Spark Hudi often uses Long for timestamps in examples, or TIMESTAMP.
                                                 // Using BIGINT to match input data.
                        case DECIMAL:
                                return "DECIMAL(10,2)"; // Defaulting to 10,2 for this specific use case
                        case STRING:
                                return "STRING";
                        default:
                                return super.mapDataTypeToSql(dataType);
                }
        }
}
