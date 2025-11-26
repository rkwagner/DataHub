package datahub.table;

import java.util.List;

import datahub.core.contract.Table;
import datahub.core.contract.TableName;
import datahub.core.contract.TableNamespace;
import datahub.core.metadata.TypedSchema;
import datahub.core.metadata.TypedSchemaImpl;
import datahub.hudi.HudiProperties;
import datahub.hudi.HudiType;
import datahub.field.City;
import datahub.field.Driver;
import datahub.field.EventTS;
import datahub.field.Fare;
import datahub.field.Rider;
import datahub.field.UUID;

public class Trips {

    public static final TypedSchema SCHEMA = new TypedSchemaImpl(List.of(
            new EventTS(),
            new UUID(),
            new Rider(),
            new Driver(),
            new Fare(),
            new City()));

    // --- 2. Define Hudi Properties (Specific Configuration) ---
    private static final HudiProperties hudiProps = new HudiProperties();

    static {
        hudiProps.setProperty(HudiType.TableProperties.TABLE_TYPE_KEY, HudiType.TableProperties.MERGE_ON_READ);
        // Use fields defined in the core schema for key identification
        hudiProps.setProperty(HudiType.TableProperties.RECORD_KEY_FIELD_KEY, new UUID().getName());
        hudiProps.setProperty(HudiType.TableProperties.PRECOMBINE_FIELD_KEY, new Rider().getName());
        hudiProps.setProperty(HudiType.TableProperties.PARTITION_FIELDS_KEY, new EventTS().getName());
    }

    public static final Table<HudiType> INSTANCE = new Table<>() {
        @Override
        public TableName getName() {
            return () -> "Trips";
        }

        @Override
        public TableNamespace getNamespace() {
            return () -> "hudi";
        }

        @Override
        public HudiProperties getProperties() {
            return hudiProps;
        }

        @Override
        public TypedSchema getSchema() {
            return SCHEMA;
        }
    };

    // TODO expect a row as a specific format to return.
    // public static final Row getRow() {
    // return
    // }
}
