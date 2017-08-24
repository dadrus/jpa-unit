package eu.drus.jpa.unit.cassandra.dataset;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParsedDataSet implements DataSet, ContentHandler {

    private Map<String, Table> tables = new HashMap<>();
    private Deque<Object> tmpDeque;

    @Override
    public Iterator<Table> iterator() {
        return tables.values().iterator();
    }

    @Override
    public Optional<Table> getTable(final String name) {
        return Optional.ofNullable(tables.get(name));
    }

    @Override
    public void onDataSetStart() {
        tmpDeque = new ArrayDeque<>();
    }

    @Override
    public void onDataSetEnd() {
        tmpDeque = null;
    }

    @Override
    public void onTableStart(final TableProperties properties) {
        tmpDeque.push(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onTableEnd() {
        final TableProperties poperties = (TableProperties) tmpDeque.pollLast();
        final List<List<RowElement>> rows = new ArrayList<>();

        Object obj;
        while ((obj = tmpDeque.pollLast()) != null) {
            rows.add((List<RowElement>) obj);
        }

        tables.put(poperties.getTableName(), new DefaultTable(poperties, rows));
    }

    @Override
    public void onRow(final List<RowElement> row) {
        tmpDeque.push(row);
    }

}
