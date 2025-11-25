package datahub.core.metadata;

/**
 * Standardized data types supported across all platforms (Hudi, Trino, Spark,
 * DB, Hive).
 * These types represent the lowest common denominator for multi-engine
 * compatibility
 * across JDBC, SQL, and Object Storage file formats (Parquet/Avro).
 */
public enum DataType {
    // --- Primitives ---
    STRING, // Universal character data (text)
    BOOLEAN, // True/False
    INT, // 32-bit signed integer
    BIGINT, // 64-bit signed integer (equivalent to SQL LONG)
    FLOAT, // 32-bit single-precision floating point
    DOUBLE, // 64-bit double-precision floating point

    // --- Temporal & Numeric Precision ---
    DATE, // Date component only (no time or time zone)
    TIMESTAMP_MICROS, // Timestamp with microsecond precision (preferred for modern lake formats)
    TIMESTAMP_MILLIS, // Timestamp with millisecond precision (required for legacy/streaming)
    DECIMAL, // High-precision numeric type (e.g., DECIMAL(10, 2))

    // --- Binary & Complex Types (Universal Support) ---
    BINARY, // Binary byte array (byte[], BLOB)
    ARRAY, // List of elements of a single type (e.g., ARRAY<INT>)
    MAP, // Key-value pairs (e.g., MAP<STRING, INT>)
    STRUCT; // Nested records/schema (equivalent to ROW or complex object)
}
