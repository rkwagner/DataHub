package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class Driver extends FieldImpl {
    private final String value;

    public Driver() {
        super("driver", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("Rob")));
        this.value = null;
    }

    public Driver(String value) {
        super("driver", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("Rob")));
        this.value = value;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}