package datahub.core;

import java.util.List;
import java.util.Optional;

import datahub.core.metadata.*;

public class TestSchemas {
    // --- 1. FIELD CONSTRAINTS (Reusable Constraint Definitions) ---
    public static final FieldConstraints NON_NULL_DEFAULT_ID = new FieldConstraintsImpl(false, Optional.of(-1L));
    public static final FieldConstraints NULLABLE_NO_DEFAULT = new FieldConstraintsImpl(true, Optional.empty());
    public static final FieldConstraints NON_NULL_DEFAULT_TIMESTAMP = new FieldConstraintsImpl(false,
            Optional.of(TestConstants.getStartTimeString()));

    // --- 2. CANONICAL DOMAIN FIELDS ---

    // Primary Key Field: ID (BIGINT, Non-Nullable)
    public static final Field ID_FIELD = new FieldImpl(
            "id",
            DataType.BIGINT,
            true,
            NON_NULL_DEFAULT_ID);

    // Attribute Field: USERNAME (STRING, Nullable)
    public static final Field USERNAME_FIELD = new FieldImpl(
            "user_name",
            DataType.STRING,
            false,
            NULLABLE_NO_DEFAULT);

    // Timestamp Field: CREATED_AT (TIMESTAMP_MICROS, Non-Nullable)
    public static final Field CREATED_AT_FIELD = new FieldImpl(
            "created_at_ts",
            DataType.TIMESTAMP_MICROS,
            false,
            NON_NULL_DEFAULT_TIMESTAMP);

    // --- 3. TYPED SCHEMAS (Reusable Schema Compositions) ---

    // Schema for a simple User Profile table
    public static final TypedSchema USER_SCHEMA = new TypedSchemaImpl(List.of(
            ID_FIELD,
            USERNAME_FIELD,
            CREATED_AT_FIELD));
}
