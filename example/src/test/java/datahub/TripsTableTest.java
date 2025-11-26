package datahub;

import datahub.core.contract.Table;
import datahub.hudi.HudiDdlGenerator;
import datahub.hudi.HudiType;
import datahub.table.TableRow;
import datahub.table.Trips;
import datahub.table.TripsRow;
import datahub.field.City;
import datahub.field.Driver;
import datahub.field.EventTS;
import datahub.field.Fare;
import datahub.field.Rider;
import datahub.field.UUID;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TripsTableTest {

    private static final Logger logger = LoggerFactory.getLogger(TripsTableTest.class);

    @Test
    public void testTripsTable() {
        logger.info("Starting Hudi Write Test...");

        // 1. Setup System
        new AppProvider().configureSystem();

        // 2. Start Spark Session
        try (SparkSession spark = new Spark.SessionBuilder().build("Hudi Write Test")) {

            // 3. Define Test Data (The "Variable" part)
            Table<HudiType> table = Trips.INSTANCE;
            List<TripsRow> data = List.of(
                    new TripsRow(
                            new EventTS(1L), new UUID("uuid1"), new Rider("rider-A"), new Driver("driver-K"),
                            new Fare(new java.math.BigDecimal("10.00")), new City("san_francisco")),
                    new TripsRow(
                            new EventTS(2L), new UUID("uuid2"), new Rider("rider-B"), new Driver("driver-L"),
                            new Fare(new java.math.BigDecimal("20.00")), new City("san_francisco")),
                    new TripsRow(
                            new EventTS(3L), new UUID("uuid3"), new Rider("rider-C"), new Driver("driver-M"),
                            new Fare(new java.math.BigDecimal("30.00")), new City("san_francisco")));

            // 4. Execute Test Flow (The "Repeatable" part)
            HudiTestSteps.TestContext context = HudiTestSteps.given(spark, table, data);
            HudiTestSteps.when(context);

            HudiTestSteps.then(context, snapshotView -> {
                // Assertions on the table contents
                long count = snapshotView.count();
                assertEquals(3, count, "Expected 3 records in the table");

                // Verify schema structure (basic check)
                assertTrue(snapshotView.schema().fieldNames().length > 0, "Schema should have fields");

                logger.info("Assertions passed: Table has {} records and valid schema.", count);
            });
        }
    }
}
