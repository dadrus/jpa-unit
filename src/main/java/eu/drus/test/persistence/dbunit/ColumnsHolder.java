package eu.drus.test.persistence.dbunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnsHolder {

    final List<String> global = new ArrayList<String>();

    final Map<String, List<String>> columnsPerTable = new HashMap<String, List<String>>();

    public ColumnsHolder(final String[] columns) {
        for (final String column : columns) {
            if (column.length() == 0) {
                continue;
            }
            if (!column.contains(".")) {
                global.add(column);
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
        List<String> tableColumns = columnsPerTable.get(tableName);

        if (tableColumns == null) {
            tableColumns = new ArrayList<String>();
            columnsPerTable.put(tableName, tableColumns);
        }

        tableColumns.add(splittedTableAndColumn[1]);
    }

}
