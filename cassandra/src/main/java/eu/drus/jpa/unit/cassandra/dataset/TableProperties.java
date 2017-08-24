package eu.drus.jpa.unit.cassandra.dataset;

import java.util.List;

public interface TableProperties {

    String getTableName();

    List<Column> getColumns();

    PrimaryKey getPrimaryKey();
}
