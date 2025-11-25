package datahub.core.metadata;

import datahub.core.TestConstants;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static datahub.core.TestSchemas.USER_SCHEMA;
import static datahub.core.TestRowUtils.validateRowIntegrity;
import static datahub.core.TestRowUtils.processRowDefaults;
import static datahub.core.TestAssertions.assertValidationFailsWithContext;

/**
 * Validates the integrity of the data processing utilities (TestRowUtils).
 * Tests focus on verifying runtime behavior: validating rows against the schema
 * constraints and correctly inserting default values.
 */
class RowDataIntegrityTest {

    // --- ROW DATA INTEGRITY & DEFAULT APPLICATION TESTS ---

    @Test
    void testRowValidationAndDefaults() {
        // Test Goal: Verify that the reusable schema accurately validates sample data
        // and correctly inserts default values during processing.

        TypedSchema schema = USER_SCHEMA;

        // 1. Test 1: Fully Compliant Row (Should Pass)
        Map<String, Object> validRow = Map.of(
                "id", 100L,
                "user_name", "alice",
                "created_at_ts", TestConstants.getStartTimeString());
        assertTrue(validateRowIntegrity(schema, validRow), "Valid row should pass integrity check.");

        // 2. Test 2: Missing Non-Nullable Field (Should Fail)
        Map<String, Object> invalidRow = Map.of(
                "user_name", "bob",
                "created_at_ts", TestConstants.getStartTimeString()
        // 'id' (Non-nullable PK) is missing
        );
        assertValidationFailsWithContext(
                () -> validateRowIntegrity(schema, invalidRow),
                "Row missing non-nullable ID should fail integrity check.",
                "Missing Primary Key ID" // Contextual message for the console output
        );

        // 3. Test 3: Inserting Defaults for Missing Nullable Field (Should Succeed)
        Map<String, Object> incompleteRow = Map.of(
                "id", 200L,
                "created_at_ts", TestConstants.getStartTimeString()
        // 'user_name' (Nullable) is missing
        );

        // Process defaults using the external utility method
        Map<String, Object> processedRow = processRowDefaults(schema, incompleteRow);

        assertTrue(processedRow.containsKey("user_name"), "Processed row must contain the nullable field.");
        assertNull(processedRow.get("user_name"),
                "Missing nullable field should be set to null by the processor.");

        // 4. Test 4: Inserting Defaults for Missing Non-Nullable Field (Checks default
        // value application)
        Map<String, Object> nonNullMissingRow = Map.of(
                "user_name", "charlie"
        // 'id' (-1L default) and 'created_at_ts' (timestamp string default) are missing
        );

        Map<String, Object> processedDefaults = processRowDefaults(schema, nonNullMissingRow);

        // Verify default insertion for ID
        assertTrue(processedDefaults.containsKey("id"));
        assertEquals(-1L, processedDefaults.get("id"),
                "Missing non-nullable ID should be set to its default -1L.");

        // Verify default insertion for Timestamp
        assertTrue(processedDefaults.containsKey("created_at_ts"));
        assertEquals(TestConstants.getStartTimeString(), processedDefaults.get("created_at_ts"),
                "Missing timestamp should use the constant default.");
    }
}
