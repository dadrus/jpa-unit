package eu.drus.jpa.unit.cassandra.dataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

public class CompositeTable implements Table {

    private TableProperties tableProperties;
    private List<Table> tables;

    public CompositeTable(final Table table1, final Table table2) {
        tableProperties = table1.getTableProperties();
        tables = Arrays.asList(table1, table2);
    }

    @Override
    public Iterator<List<RowElement>> iterator() {
        return Iterables.concat(tables).iterator();
    }

    @Override
    public TableProperties getTableProperties() {
        return tableProperties;
    }

    @Override
    public int getRowCount() {
        return tables.stream().mapToInt(Table::getRowCount).sum();
    }

    @Override
    public List<RowElement> getRow(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " < 0 ");
        }

        int totalCount = 0;
        for (final Table t : tables) {
            final int count = t.getRowCount();
            if (totalCount + count > index) {
                return t.getRow(index - totalCount);
            }
            totalCount += count;
        }

        throw new IndexOutOfBoundsException(index + " > " + totalCount);
    }

}
