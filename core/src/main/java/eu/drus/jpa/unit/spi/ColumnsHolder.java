package eu.drus.jpa.unit.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ColumnsHolder {

    private final List<String> columns = new ArrayList<>();

    private final Map<String, List<String>> columnsInTable = new HashMap<>();

    public ColumnsHolder(final String[] columnNames, final Function<String, String> idMapper) {
        for (final String columnName : columnNames) {
            if (columnName.length() == 0) {
                throw new IllegalArgumentException("Column name can not be an empty string");
            }
            if (!columnName.contains(".")) {
                columns.add(idMapper.apply(columnName));
            } else {
                splitTableAndColumn(columnName, idMapper);
            }
        }
    }

    private void splitTableAndColumn(final String columnNameToExclude, final Function<String, String> idMapper) {
        final String[] splittedTableAndColumnNames = columnNameToExclude.split("\\.");

        if (splittedTableAndColumnNames.length != 2) {
            throw new IllegalArgumentException(
                    "Cannot associated table with column for [" + columnNameToExclude + "]. Expected format: 'tableName.columnName'");
        }

        final String tableName = splittedTableAndColumnNames[0];
        List<String> tableColumnNames = columnsInTable.get(tableName);

        if (tableColumnNames == null) {
            tableColumnNames = new ArrayList<>();
            columnsInTable.put(tableName, tableColumnNames);
        }

        tableColumnNames.add(idMapper.apply(splittedTableAndColumnNames[1]));
    }

    public List<String> getColumns(final String tableName) {
        final List<String> tableColumns = columnsInTable.get(tableName);

        final List<String> result = new ArrayList<>();
        result.addAll(columns);
        result.addAll(tableColumns != null ? tableColumns : Collections.emptyList());
        return result;
    }

}
