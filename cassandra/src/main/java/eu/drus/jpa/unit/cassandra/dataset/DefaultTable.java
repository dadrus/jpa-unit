package eu.drus.jpa.unit.cassandra.dataset;

import java.util.Iterator;
import java.util.List;

public class DefaultTable implements Table {

    private TableProperties properties;
    private List<List<RowElement>> rows;

    public DefaultTable(final TableProperties properties, final List<List<RowElement>> rows) {
        this.properties = properties;
        this.rows = rows;
    }

    @Override
    public Iterator<List<RowElement>> iterator() {
        return rows.iterator();
    }

    @Override
    public TableProperties getTableProperties() {
        return properties;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public List<RowElement> getRow(final int index) {
        return rows.get(index);
    }

}
