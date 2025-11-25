package datahub.core.metadata;

import java.util.Map;
import java.util.Optional;

/**
 * Defines functions for cross-platform schema interaction, including
 * serialization and sample data generation.
 * All concrete schemas (TypedSchemaImpl) should implement this contract.
 */
public interface SchemaInteroperability {

    // --- 1. AVRO/PARQUET SCHEMA MANAGEMENT ---

    /**
     * Creates a standard Avro Schema representation of this definition.
     * This is used by Spark, Hudi, and Trino for metadata exchange and file
     * writing.
     * 
     * @return The Avro Schema object (implementation specific, usually String or
     *         org.apache.avro.Schema).
     */
    Optional<String> toAvroSchemaString();

    /**
     * Validates an external Avro Schema string against this definition.
     * Used during runtime to check if an incoming data file matches the expected
     * table structure.
     * 
     * @param externalAvroSchema The schema string from the file to check.
     * @return True if the schemas are compatible (same types, same field names),
     *         false otherwise.
     */
    boolean checkCompatibility(String externalAvroSchema);

    // --- 2. TEST DATA & ROW MANAGEMENT ---

    /**
     * Creates a single, valid sample data row (Map) that adheres to this schema.
     * Non-nullable fields with defaults should use them; others are set to
     * representative non-null values.
     * This is crucial for unit tests and synthetic data generation.
     * 
     * @return A {@code Map<String, Object>} representing a sample row.
     */
    Map<String, Object> createSampleRow();
}