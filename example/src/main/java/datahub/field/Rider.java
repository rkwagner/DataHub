package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class Rider extends FieldImpl {
    private final String value;

    public Rider() {
        super("rider", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("Zombie")));
        this.value = null;
    }

    public Rider(String value) {
        super("rider", DataType.STRING, false, new FieldConstraintsImpl(false, Optional.of("Zombie")));
        this.value = value;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}