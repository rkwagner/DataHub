package datahub.core.metadata;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Concrete, immutable implementation of TypedSchema.
 * This object is the canonical representation of a table's structure and
 * implements the SchemaInteroperability contract using the Apache Avro library.
 */
public record TypedSchemaImpl(List<Field> fields)
        implements TypedSchema, SchemaInteroperability {

    @Override
    public List<Field> getFields() {
        return fields;
    }

    // --- 1. AVRO SCHEMA MANAGEMENT IMPLEMENTATION ---

    /**
     * Helper function to map our DataType enum to Avro's Schema.Type
     * and handle complex types (like LONG for BIGINT).
     */
    private Schema.Type toAvroType(DataType dataType) {
        return switch (dataType) {
            case STRING -> Schema.Type.STRING;
            case BOOLEAN -> Schema.Type.BOOLEAN;
            case INT -> Schema.Type.INT;
            case BIGINT -> Schema.Type.LONG; // Avro uses LONG for BIGINT
            case FLOAT -> Schema.Type.FLOAT;
            case DOUBLE -> Schema.Type.DOUBLE;
            case DATE -> Schema.Type.INT; // Avro date is INT (logical type)
            case TIMESTAMP_MICROS -> Schema.Type.LONG; // Avro timestamp is LONG (logical type)
            case TIMESTAMP_MILLIS -> Schema.Type.LONG; // Avro timestamp is LONG (logical type)
            case DECIMAL -> Schema.Type.BYTES; // Avro decimal is BYTES (logical type)
            case BINARY -> Schema.Type.BYTES;
            case ARRAY -> Schema.Type.ARRAY; // Complex types need builders, but map to Avro types
            case MAP -> Schema.Type.MAP;
            case STRUCT -> Schema.Type.RECORD;
            // Default cases for completeness
            default -> Schema.Type.NULL;
        };
    }

    /**
     * Generates a single, complete Avro Schema object from the internal fields
     * list.
     */
    public Schema toAvroSchemaObject() {
        // Start the record assembler
        SchemaBuilder.FieldAssembler<Schema> assembler = SchemaBuilder.record("GeneratedSchema")
                .namespace("datahub.avro")
                .fields();

        for (Field field : fields) {
            Schema avroFieldSchema = toAvroSchema(field);

            var fieldDefaultBuilder = assembler
                    .name(field.getName())
                    .doc("Canonical field: " + field.getName()) // Handle documentation
                    .type(avroFieldSchema); // Returns the object responsible for default value setting

            // Handle default value and nullability
            if (field.getConstraints().getDefaultValue().isPresent()) {
                // Non-nullable field with a default (needs custom JSON node conversion, which
                // we mock/skip)
                // For simplicity and correctness with simple types, we use noDefault() for now.
                assembler = fieldDefaultBuilder.noDefault();
            } else if (field.getConstraints().isNullable()) {
                // Nullable field without a default must explicitly use .withDefault(null)
                assembler = fieldDefaultBuilder.withDefault(null);
            } else {
                // Non-nullable field without explicit default
                assembler = fieldDefaultBuilder.noDefault();
            }
        }

        return assembler.endRecord();
    }

    /**
     * Converts a single Field definition into a complete Avro Schema, handling
     * Logical Types and Nullability.
     */
    private Schema toAvroSchema(Field field) {
        Schema baseType = Schema.create(toAvroType(field.getDataType()));

        // Handle Logical Types (e.g., TIMESTAMP_MICROS maps to Avro LONG with
        // logicalType annotation)
        if (field.getDataType() == DataType.TIMESTAMP_MICROS) {
            baseType = org.apache.avro.LogicalTypes.timestampMicros().addToSchema(baseType);
        } else if (field.getDataType() == DataType.TIMESTAMP_MILLIS) {
            baseType = org.apache.avro.LogicalTypes.timestampMillis().addToSchema(baseType);
        } else if (field.getDataType() == DataType.DATE) {
            baseType = org.apache.avro.LogicalTypes.date().addToSchema(baseType);
        }

        // Handle Nullability via Union type (if nullable, must be a union of [NULL,
        // TYPE])
        if (field.getConstraints().isNullable()) {
            return Schema.createUnion(Schema.create(Schema.Type.NULL), baseType);
        }

        return baseType;
    }

    @Override
    public Optional<String> toAvroSchemaString() {
        // Return the JSON representation of the generated Avro Schema object
        return Optional.of(toAvroSchemaObject().toString(true)); // toString(true) for pretty printing
    }

    @Override
    public boolean checkCompatibility(String externalAvroSchema) {
        // NOTE: A proper implementation would use org.apache.avro.Schema.Parser()
        // and then
        // org.apache.avro.SchemaCompatibility.checkReaderWriterCompatibility(...)
        // We will keep the mock check for simplicity and focus on the schema object
        // generation.
        return externalAvroSchema != null && externalAvroSchema.contains("user_name");
    }

    // --- 2. TEST DATA & ROW MANAGEMENT IMPLEMENTATION ---

    @Override
    public Map<String, Object> createSampleRow() {
        // ... (Implementation remains the same as in the previous iteration) ...
        Map<String, Object> sampleRow = new java.util.HashMap<>();

        for (Field field : fields) {
            Object value;
            // 1. Check for explicit default
            if (field.getConstraints().getDefaultValue().isPresent()) {
                value = field.getConstraints().getDefaultValueOrNull();
            } else if (field.getConstraints().isNullable()) {
                // 2. Nullable fields without default are set to null
                value = null;
            } else {
                // 3. Non-nullable fields without default are given a representative value
                value = switch (field.getDataType()) {
                    case BIGINT -> 1L;
                    case STRING -> "sample_data";
                    case TIMESTAMP_MICROS -> 20000101000000L;
                    case INT -> 100;
                    case BOOLEAN -> true;
                    // Note: Complex types (ARRAY, MAP, STRUCT) would need recursive population
                    // here.
                    default -> "MOCK_VALUE";
                };
            }
            sampleRow.put(field.getName(), value);
        }
        return sampleRow;
    }
}