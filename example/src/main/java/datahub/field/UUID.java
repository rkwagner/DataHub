package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class UUID extends FieldImpl {
    private final String value;

    public UUID() {
        super("uuid", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("0x54b")));
        this.value = null;
    }

    public UUID(String value) {
        super("uuid", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("0x54b")));
        this.value = value;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}