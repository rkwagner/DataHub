package datahub.core;

import datahub.core.metadata.Field;
import datahub.core.metadata.TypedSchema;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Utility class to simplify row manipulation tasks in unit tests, such as
 * validating data integrity against a schema or applying default values.
 */
public class TestRowUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestRowUtils.class);

    /**
     * Validates a sample row (Map) against the provided TypedSchema.
     * Checks primarily for the presence of non-nullable fields.
     * 
     * @param schema The schema to validate against.
     * @param row    The sample row data (key=field name, value=field value).
     * @return True if the row meets all non-nullable constraints, false otherwise.
     */
    public static boolean validateRowIntegrity(TypedSchema schema, Map<String, Object> row) {
        for (Field field : schema.getFields()) {
            // Check 1: Non-nullable fields must be present
            if (!field.getConstraints().isNullable() && !row.containsKey(field.getName())) {
                logger.error("Validation failed: Missing non-nullable field: " + field.getName());
                return false;
            }
            // Add more complex checks here (e.g., type matching, format validation)
        }
        return true;
    }

    /**
     * Processes an incomplete row by filling in default values for missing fields
     * based on the schema's FieldConstraints.
     * 
     * @param schema The schema defining default values.
     * @param row    The input row with missing values.
     * @return A new Map containing all fields, with defaults applied where missing.
     */
    public static Map<String, Object> processRowDefaults(TypedSchema schema, Map<String, Object> row) {
        Map<String, Object> output = new HashMap<>(row);
        for (Field field : schema.getFields()) {
            if (!output.containsKey(field.getName())) {
                // Apply defined default value (which may be null for nullable fields)
                output.put(field.getName(), field.getConstraints().getDefaultValueOrNull());
            }
        }
        return output;
    }
}