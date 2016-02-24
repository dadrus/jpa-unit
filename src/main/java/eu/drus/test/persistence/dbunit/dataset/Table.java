package eu.drus.test.persistence.dbunit.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table {
    private final String tableName;

    private final Set<String> columns = new HashSet<String>();

    private final List<Row> rows = new ArrayList<Row>();

    public Table(final String tableName) {
        this.tableName = tableName;
    }

    public void addRows(final Collection<Row> rows) {
        this.rows.addAll(rows);
    }

    public void addColumns(final Collection<String> columns) {
        this.columns.addAll(columns);
    }

    public String getTableName() {
        return tableName;
    }

    public Set<String> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
