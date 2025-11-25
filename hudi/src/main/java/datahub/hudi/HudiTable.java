package datahub.hudi;

import datahub.core.contract.AbstractTable;
import datahub.core.contract.Table;
import datahub.core.contract.TableName;
import datahub.core.contract.TableNamespace;
import datahub.core.contract.Properties;
import datahub.core.metadata.TypedSchema;
import datahub.core.metadata.Field;
import datahub.core.exceptions.SchemaValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The final immutable Hudi Table implementation.
 * It uses the AbstractTable base class to enforce validation upon construction.
 */
public class HudiTable extends AbstractTable<HudiType> {

    // Static lists to ensure they are available during super constructor execution
    private static final List<String> requiredProperties = List.of(
            HudiType.TableProperties.TABLE_TYPE_KEY,
            HudiType.TableProperties.RECORD_KEY_FIELD_KEY,
            HudiType.TableProperties.PRECOMBINE_FIELD_KEY,
            HudiType.TableProperties.PARTITION_FIELDS_KEY);

    private static final List<String> fieldProperties = List.of(
            HudiType.TableProperties.RECORD_KEY_FIELD_KEY,
            HudiType.TableProperties.PRECOMBINE_FIELD_KEY,
            HudiType.TableProperties.PARTITION_FIELDS_KEY);

    // Constructor is protected; only the Builder can call it.
    protected HudiTable(
            TableName name,
            TableNamespace namespace,
            TypedSchema schema,
            Properties<HudiType> properties) throws SchemaValidationException {

        super(name, namespace, schema, properties);

        if (!(properties instanceof Properties)) {
            throw new IllegalArgumentException(
                    "Internal Error: Properties object is not a valid Properties implementation.");
        }
        // No need to store hudiProperties separately, we can cast this.properties when
        // needed
    }

    private void validateFieldProperties() {
        Set<String> schemaFieldNames = schema.getFields().stream()
                .map(Field::getName)
                .collect(Collectors.toSet());

        HudiProperties hudiProps = (HudiProperties) this.properties;

        for (String prop : requiredProperties) {
            if (hudiProps.getStringProperty(prop) == null) {
                throw new SchemaValidationException("Hudi Validation Failed: Mandatory property " + prop
                        + " is missing in table " + this.name.getValue());
            }

            if (fieldProperties.contains(prop)) {
                validatePropertyFields(prop, schemaFieldNames);
            }
        }
    }

    private void validatePropertyFields(String propertyKey, Set<String> schemaFieldNames) {
        HudiProperties hudiProps = (HudiProperties) this.properties;
        String propValue = hudiProps.getStringProperty(propertyKey);

        if (propValue != null && !propValue.isEmpty()) {
            // Handle comma-separated list of keys (especially for PARTITION_FIELDS_KEY)
            Set<String> fieldsToCheck = Arrays.stream(propValue.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            for (String fieldName : fieldsToCheck) {
                if (!schemaFieldNames.contains(fieldName)) {
                    throw new SchemaValidationException(
                            String.format(
                                    "Hudi Validation Failed: Field '%s' defined in property '%s' is not present in the canonical table schema '%s'.",
                                    fieldName, propertyKey, this.name.getValue()));
                }
            }
        }
    }

    private void validateOtherProperties() {
        HudiProperties hudiProps = (HudiProperties) this.properties;
        // Ensure table type is set
        if (hudiProps.getStringProperty(HudiType.TableProperties.TABLE_TYPE_KEY) == null) {
            throw new SchemaValidationException("Hudi Validation Failed: Mandatory property "
                    + HudiType.TableProperties.TABLE_TYPE_KEY + " is missing.");
        }
    }

    private void validateTimestampConsistency() {
        HudiProperties hudiProps = (HudiProperties) this.properties;
        String enforcedPrecision = hudiProps.getStringProperty(HudiProperties.TIMESTAMP_PRECISION_KEY);

        boolean hasMillis = false;
        boolean hasMicros = false;

        for (Field field : schema.getFields()) {
            if (field.getDataType() == datahub.core.metadata.DataType.TIMESTAMP_MILLIS) {
                hasMillis = true;
            } else if (field.getDataType() == datahub.core.metadata.DataType.TIMESTAMP_MICROS) {
                hasMicros = true;
            }
        }

        // 1. Check for consistency (cannot mix types)
        if (hasMillis && hasMicros) {
            throw new SchemaValidationException(
                    "Hudi Validation Failed: Inconsistent timestamp precision. Table cannot contain both TIMESTAMP_MILLIS and TIMESTAMP_MICROS fields.");
        }

        // 2. Check for enforcement (if property is set)
        if (enforcedPrecision != null) {
            if ("MILLIS".equalsIgnoreCase(enforcedPrecision) && hasMicros) {
                throw new SchemaValidationException(
                        "Hudi Validation Failed: Timestamp precision enforced to MILLIS, but TIMESTAMP_MICROS field found.");
            }
            if ("MICROS".equalsIgnoreCase(enforcedPrecision) && hasMillis) {
                throw new SchemaValidationException(
                        "Hudi Validation Failed: Timestamp precision enforced to MICROS, but TIMESTAMP_MILLIS field found.");
            }
        }
    }

    /**
     * This is the override that satisfies the compiler (avoiding the erasure
     * clash).
     */
    @Override
    protected void validateConstruction(Table<HudiType> finalTable) throws SchemaValidationException {
        validateFieldProperties();
        validateOtherProperties();
        validateTimestampConsistency();
    }
}