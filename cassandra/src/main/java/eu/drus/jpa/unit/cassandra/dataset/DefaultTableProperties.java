package eu.drus.jpa.unit.cassandra.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultTableProperties implements TableProperties {

    private String tableName;
    private Map<String, Column> columns;
    private PrimaryKey primaryKey;

    public DefaultTableProperties(final String tableName) {
        this(tableName, new HashMap<>(), PrimaryKey.builder().build());
    }

    public DefaultTableProperties(final String tableName, final Map<String, Column> columns, final PrimaryKey primaryKey) {
        this.tableName = tableName;
        this.columns = columns;
        this.primaryKey = primaryKey;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<Column> getColumns() {
        return new ArrayList<>(columns.values());
    }

    @Override
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public Column addColumnIfAbsent(final String name) {
        return columns.computeIfAbsent(name, Column::new);
    }

}
