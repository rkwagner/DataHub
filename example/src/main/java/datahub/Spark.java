package datahub;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import datahub.core.metadata.DataType;
import datahub.core.metadata.TypedSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Spark {
    public static class SessionBuilder {
        public SparkSession build(String appName) {
            String minioEndpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://host.docker.internal:9000");
            String minioAccessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minio");
            String minioSecretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minio123");

            return SparkSession.builder()
                    .appName(appName)
                    .master("local[*]")
                    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                    .config("spark.hadoop.fs.s3a.endpoint", minioEndpoint)
                    .config("spark.hadoop.fs.s3a.access.key", minioAccessKey)
                    .config("spark.hadoop.fs.s3a.secret.key", minioSecretKey)
                    .config("spark.hadoop.fs.s3a.path.style.access", "true")
                    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                    .config("spark.hadoop.fs.s3a.connection.establish.timeout", "5000")
                    .config("spark.hadoop.fs.s3a.connection.timeout", "10000")
                    .config("spark.hadoop.fs.s3a.connection.ssl.enabled", "false")
                    .config("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionExtension")
                    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
                    .getOrCreate();
        }
    }

    public static class Writer {
        private static final Logger logger = LoggerFactory.getLogger(Writer.class);

        public void write(Dataset<Row> df, String tableName, String basePath) {
            logger.info("Inserting data...");
            df.write()
                    .format("hudi")
                    .options(getQuickWriteOptions(tableName))
                    .mode(SaveMode.Append)
                    .save(basePath);
        }

        private static Map<String, String> getQuickWriteOptions(String tableName) {
            Map<String, String> options = new HashMap<>();
            options.put("hoodie.table.name", tableName);
            options.put("hoodie.datasource.write.recordkey.field", "uuid");
            options.put("hoodie.datasource.write.partitionpath.field", "event_ts");
            options.put("hoodie.datasource.write.precombine.field", "rider");
            return options;
        }
    }

    public static class Reader {
        public Dataset<Row> read(SparkSession spark, String path) {
            return spark.read().format("hudi").load(path);
        }
    }

    public static class Schema {
        public static StructType toSparkSchema(TypedSchema typedSchema) {
            List<StructField> fields = typedSchema.getFields().stream()
                    .map(field -> DataTypes.createStructField(
                            field.getName(),
                            mapDataType(field.getDataType()),
                            true)) // Allow nulls for simplicity
                    .collect(Collectors.toList());
            return DataTypes.createStructType(fields);
        }

        private static org.apache.spark.sql.types.DataType mapDataType(DataType dataType) {
            switch (dataType) {
                case STRING:
                    return DataTypes.StringType;
                case INT:
                    return DataTypes.IntegerType;
                case BIGINT:
                case TIMESTAMP_MICROS: // Mapping TIMESTAMP_MICROS to Long for now as EventTS holds a Long
                    return DataTypes.LongType;
                case BOOLEAN:
                    return DataTypes.BooleanType;
                case DECIMAL:
                    return DataTypes.createDecimalType(10, 2); // Default precision/scale
                default:
                    return DataTypes.StringType;
            }
        }
    }
}
