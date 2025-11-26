package datahub.field;

import datahub.core.metadata.FieldImpl;
import java.util.Optional;
import datahub.core.metadata.DataType;
import datahub.core.metadata.FieldConstraintsImpl;

public class EventTS extends FieldImpl {
    private final Long value;

    public EventTS() {
        super("event_ts", DataType.TIMESTAMP_MICROS, false, new FieldConstraintsImpl(false, Optional.of(-1L)));
        this.value = null;
    }

    public EventTS(Long value) {
        super("event_ts", DataType.TIMESTAMP_MICROS, false, new FieldConstraintsImpl(false, Optional.of(-1L)));
        this.value = value;
    }

    public Optional<Long> getValue() {
        return Optional.ofNullable(value);
    }
}