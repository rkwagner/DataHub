package datahub;

import datahub.core.contract.Table;
import datahub.hudi.HudiDdlGenerator;
import datahub.hudi.HudiType;
import datahub.table.TableRow;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;

public class HudiTestSteps {

    private static final Logger logger = LoggerFactory.getLogger(HudiTestSteps.class);

    public static class TestContext {
        public final SparkSession spark;
        public final String tableName;
        public final String basePath;
        public final Dataset<Row> dataFrame;

        public TestContext(SparkSession spark, String tableName, String basePath, Dataset<Row> dataFrame) {
            this.spark = spark;
            this.tableName = tableName;
            this.basePath = basePath;
            this.dataFrame = dataFrame;
        }
    }

    public static TestContext given(SparkSession spark, Table<HudiType> table, List<? extends TableRow> dataRows) {
        AppProvider appProvider = new AppProvider();
        String tableName = table.getName().getValue();
        String basePath = appProvider.getBasePath(tableName);

        // Generate DDL and Create Table
        logger.info("Generating DDL for {}...", tableName);
        HudiDdlGenerator ddlGenerator = new HudiDdlGenerator();
        String ddl = ddlGenerator.generateCreateStatement(table);
        ddl += " LOCATION '" + basePath + "'";

        logger.info("Executing DDL:\n{}", ddl);
        spark.sql(ddl);

        // Prepare Data
        List<Row> rows = dataRows.stream().map(TableRow::getRow).collect(Collectors.toList());
        StructType schema = Spark.Schema.toSparkSchema(table.getSchema());
        Dataset<Row> df = spark.createDataFrame(rows, schema);

        return new TestContext(spark, tableName, basePath, df);
    }

    public static void when(TestContext context) {
        // Write Data
        new Spark.Writer().write(context.dataFrame, context.tableName, context.basePath);
        logger.info("Successfully wrote Hudi table to {}", context.basePath);
    }

    public static void then(TestContext context, Consumer<Dataset<Row>> assertion) {
        // Verify Data
        logger.info("Reading Snapshot View to verify latest state...");
        Dataset<Row> snapshotView = new Spark.Reader().read(context.spark, context.basePath);

        long count = snapshotView.count();

        if (count > 0) {
            logger.info("SUCCESS: Snapshot View read. Total records: {}", count);
            snapshotView.show(5);
        } else {
            logger.error("FAILURE: Snapshot View returned 0 records.");
        }

        // Execute custom assertion
        assertion.accept(snapshotView);
    }
}
