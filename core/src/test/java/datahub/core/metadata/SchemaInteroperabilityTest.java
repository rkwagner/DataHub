package datahub.core.metadata;

import org.junit.jupiter.api.Test;
import org.apache.avro.Schema; // Import Avro Schema
import org.apache.avro.Schema.Type; // Import Avro Type

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static datahub.core.TestSchemas.USER_SCHEMA;

/**
 * Tests the SchemaInteroperability contract, which ensures the TypedSchema
 * can perform essential tasks like Avro serialization and sample data
 * generation.
 * This test validates the functionality required by runtime engines (Spark,
 * Trino).
 */
class SchemaInteroperabilityTest {

    private final SchemaInteroperability schema = (SchemaInteroperability) USER_SCHEMA;

    // --- 1. AVRO REPRESENTATION TESTS ---

    @Test
    void testToAvroSchemaString() {
        Optional<String> avroSchemaString = schema.toAvroSchemaString();

        assertTrue(avroSchemaString.isPresent(), "Avro schema must be generated.");
        String schemaStr = avroSchemaString.get();

        Schema generatedSchema;
        try {
            // Use the Avro Parser to read the generated JSON string
            generatedSchema = new Schema.Parser().parse(schemaStr);
        } catch (Exception e) {
            fail("Failed to parse generated Avro schema string into object: " + e.getMessage(), e);
            return;
        }

        // Assert Avro structure basics using the parsed object
        // This is the most reliable check for the root element type:
        assertEquals(Type.RECORD, generatedSchema.getType(), "The root schema type must be 'RECORD'.");

        // Assert key fields are present
        assertNotNull(generatedSchema.getField("id"), "ID field must be present in the generated schema.");
        assertNotNull(generatedSchema.getField("user_name"), "User name field must be present.");
        assertNotNull(generatedSchema.getField("created_at_ts"), "Timestamp field must be present.");
    }

    @Test
    void testCheckCompatibility() {
        // Mock a simple, compatible external schema (Trino/Hive metadata)
        String externalSchema = "{\"type\":\"record\", \"name\":\"External\", \"fields\": [{\"name\":\"user_name\"}]}";

        assertTrue(schema.checkCompatibility(externalSchema), "Schemas should be compatible based on mock logic.");

        // Mock an incompatible external schema
        String incompatibleSchema = "{\"type\":\"record\", \"name\":\"External\", \"fields\": [{\"name\":\"bad_field\"}]}";

        assertFalse(schema.checkCompatibility(incompatibleSchema), "Incompatible schema should fail check.");
    }

    // --- 2. SAMPLE DATA GENERATION TESTS ---

    @Test
    void testCreateSampleRow() {
        Map<String, Object> sampleRow = schema.createSampleRow();

        assertEquals(3, sampleRow.size(), "Sample row must contain exactly 3 fields.");

        // 1. Check PK/Non-Nullable field with Default Value (-1L)
        assertTrue(sampleRow.containsKey("id"), "Sample row must contain the ID field.");
        assertEquals(-1L, sampleRow.get("id"), "ID field must use the non-nullable default value.");

        // 2. Check Nullable field without Default Value (Should be null)
        assertTrue(sampleRow.containsKey("user_name"), "Sample row must contain user_name.");
        assertNull(sampleRow.get("user_name"),
                "User name field should be set to null as it's nullable with no default.");

        // 3. Check Timestamp field with Calculated Default
        assertTrue(sampleRow.containsKey("created_at_ts"), "Sample row must contain the timestamp field.");
        // The value should be the constant string from TestConstants
        assertTrue(sampleRow.get("created_at_ts") instanceof String, "Timestamp should be a String.");
    }
}