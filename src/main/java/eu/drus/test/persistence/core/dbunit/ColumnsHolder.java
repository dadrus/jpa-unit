package eu.drus.test.persistence.core.dbunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnsHolder {

    private final List<String> columns = new ArrayList<>();

    private final Map<String, List<String>> columnsInTable = new HashMap<>();

    public ColumnsHolder(final String[] columns) {
        for (final String column : columns) {
            if (column.length() == 0) {
                continue;
            }
            if (!column.contains(".")) {
                this.columns.add(column);
            } else {
                splitTableAndColumn(column);
            }
        }
    }

    private void splitTableAndColumn(final String columnToExclude) {
        final String[] splittedTableAndColumn = columnToExclude.split("\\.");

        if (splittedTableAndColumn.length != 2) {
            throw new IllegalArgumentException(
                    "Cannot associated table with column for [" + columnToExclude + "]. Expected format: 'tableName.columnName'");
        }

        final String tableName = splittedTableAndColumn[0];
        List<String> columns = columnsInTable.get(tableName);

        if (columns == null) {
            columns = new ArrayList<>();
            columnsInTable.put(tableName, columns);
        }

        columns.add(splittedTableAndColumn[1]);
    }

    public List<String> getColumns(final String tableName) {
        final List<String> tableColumns = columnsInTable.get(tableName);

        final List<String> result = new ArrayList<>();
        result.addAll(columns);
        result.addAll(tableColumns != null ? tableColumns : Collections.emptyList());
        return result;
    }

}
