package datahub.core;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Centralized constants for testing purposes.
 */
public class TestConstants {
    public static final String TEST_TABLE_NAME = "test_table";
    public static final String TEST_NAMESPACE = "test_namespace";
    public static final String HUDI_TABLE_NAME = "hudi_table";
    public static final String DATA_LAKE_NAMESPACE = "data_lake";

    public static final Timestamp START_TIMESTAMP = Timestamp.valueOf("2000-01-01 00:00:00");
    private static String START_TIME_STRING = getTimeString(START_TIMESTAMP);

    public static String getStartTimeString() {
        return START_TIME_STRING;
    }

    private static String getTimeString(Timestamp timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return timestamp.toLocalDateTime().format(formatter);
    }
}
