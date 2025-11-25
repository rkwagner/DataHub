package datahub.core.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static datahub.core.TestSchemas.*;

/**
 * Validates the core structural metadata definitions created by the TestSchemas
 * factory.
 * This ensures that Domain Fields (Field) are instantiated with the correct
 * types and constraints,
 * guaranteeing the integrity of the canonical schema definitions.
 */
class SchemaDefinitionTest {

    @Test
    void testCanonicalSchemaStructure() {
        // Test Goal: Ensure the canonical USER_SCHEMA is defined correctly in the
        // factory.
        TypedSchema userSchema = USER_SCHEMA;

        // 1. Validate size and order
        assertEquals(3, userSchema.getFields().size(), "Schema must have exactly 3 fields defined.");
        assertEquals(ID_FIELD, userSchema.getFields().get(0), "ID field must be the first field.");

        // 2. Validate Primary Key Constraints (ID Field)
        Field idField = userSchema.getFields().stream()
                .filter(Field::isPrimaryKey)
                .findFirst().orElseThrow();

        assertTrue(idField.isPrimaryKey(), "ID must be marked as Primary Key.");
        assertFalse(idField.getConstraints().isNullable(), "Primary Key must be non-nullable.");
        assertEquals(DataType.BIGINT, idField.getDataType(), "ID must be BIGINT type.");
        assertEquals(-1L, idField.getConstraints().getDefaultValueOrNull(),
                "Non-nullable ID must have a default.");

        // 3. Validate Nullable Field Constraints (Username Field)
        Field usernameField = userSchema.getFields().stream()
                .filter(f -> f.getName().equals("user_name"))
                .findFirst().orElseThrow();

        assertTrue(usernameField.getConstraints().isNullable(), "Username should be nullable.");
        assertTrue(usernameField.getConstraints().getDefaultValue().isEmpty(),
                "Nullable field should have no default value defined.");
    }
}
