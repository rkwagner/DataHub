package datahub.table;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import datahub.field.City;
import datahub.field.Driver;
import datahub.field.EventTS;
import datahub.field.Fare;
import datahub.field.Rider;
import datahub.field.UUID;

public class TripsRow implements TableRow {
    private final EventTS eventTS;
    private final UUID uuid;
    private final Rider rider;
    private final Driver driver;
    private final Fare fare;
    private final City city;

    public TripsRow(EventTS eventTS, UUID uuid, Rider rider, Driver driver, Fare fare, City city) {
        this.eventTS = eventTS;
        this.uuid = uuid;
        this.rider = rider;
        this.driver = driver;
        this.fare = fare;
        this.city = city;
    }

    @Override
    public Row getRow() {
        return RowFactory.create(
                eventTS.getValue().orElse(null),
                uuid.getValue().orElse(null),
                rider.getValue().orElse(null),
                driver.getValue().orElse(null),
                fare.getValue().orElse(null),
                city.getValue().orElse(null));
    }
}
