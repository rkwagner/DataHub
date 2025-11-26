package datahub.table;

import org.apache.spark.sql.Row;

public interface TableRow {
    Row getRow();
}
