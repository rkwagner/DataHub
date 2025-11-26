package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class Fare extends FieldImpl {
    private final java.math.BigDecimal value;

    public Fare() {
        super("fare", DataType.DECIMAL, false, new FieldConstraintsImpl(false, Optional.of(3.50)));
        this.value = null;
    }

    public Fare(java.math.BigDecimal value) {
        super("fare", DataType.DECIMAL, false, new FieldConstraintsImpl(false, Optional.of(3.50)));
        this.value = value;
    }

    public Optional<java.math.BigDecimal> getValue() {
        return Optional.ofNullable(value);
    }
}