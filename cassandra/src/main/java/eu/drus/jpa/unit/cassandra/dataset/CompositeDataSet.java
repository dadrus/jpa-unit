package eu.drus.jpa.unit.cassandra.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class CompositeDataSet implements DataSet {

    private Map<String, Table> tablesMap;

    public CompositeDataSet(final Collection<DataSet> dataSets) {
        tablesMap = new HashMap<>();

        dataSets.forEach(ds -> ds.forEach(this::addTable));
    }

    private void addTable(final Table newTable) {
        final String tableName = newTable.getTableProperties().getTableName();

        final Optional<Table> table = getTable(tableName);
        if (table.isPresent()) {
            // Merge existing and new tables together
            tablesMap.put(tableName, new CompositeTable(table.get(), newTable));
        } else {
            // Add new table
            tablesMap.put(tableName, newTable);
        }
    }

    @Override
    public Iterator<Table> iterator() {
        return tablesMap.values().iterator();
    }

    @Override
    public Optional<Table> getTable(final String name) {
        return Optional.ofNullable(tablesMap.get(name));
    }

}
