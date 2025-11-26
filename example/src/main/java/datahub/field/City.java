package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class City extends FieldImpl {
    private final String value;

    public City() {
        super("city", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("San Diego")));
        this.value = null;
    }

    public City(String value) {
        super("city", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("San Diego")));
        this.value = value;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}