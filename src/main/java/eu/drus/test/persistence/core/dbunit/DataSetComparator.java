package eu.drus.test.persistence.core.dbunit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DiffCollectingFailureHandler;
import org.dbunit.assertion.Difference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.FilteredTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.IncludeTableFilter;

import eu.drus.test.persistence.core.AssertionErrorCollector;

public class DataSetComparator {

    private static final String DIFF_ERROR = "%s | In row %d: expected value of %s \"%s\" but was \"%s\".";

    private static final Logger log = Logger.getLogger(DataSetComparator.class.getName());

    private final ColumnsHolder toExclude;

    private final ColumnsHolder orderBy;

    private final Set<Class<? extends IColumnFilter>> columnFilters;

    public DataSetComparator(final String[] orderBy, final String[] toExclude, final Set<Class<? extends IColumnFilter>> columnFilters) {
        this.toExclude = new ColumnsHolder(toExclude);
        this.orderBy = new ColumnsHolder(orderBy);
        this.columnFilters = columnFilters;
    }

    public void compare(final IDataSet currentDataSet, final IDataSet expectedDataSet, final AssertionErrorCollector errorCollector)
            throws DatabaseUnitException, ReflectiveOperationException {
        if (expectedDataSet.getTableNames().length == 0) {
            shouldBeEmpty(currentDataSet, errorCollector);
        } else {
            compareContent(currentDataSet, expectedDataSet, errorCollector);
        }
    }

    private void shouldBeEmpty(final IDataSet dataSet, final AssertionErrorCollector errorCollector) throws DatabaseUnitException {
        for (final String tableName : dataSet.getTableNames()) {
            final int rowCount = dataSet.getTable(tableName).getRowCount();
            if (rowCount != 0) {
                errorCollector.collect("Table " + tableName + " was expected to be empty, but has <" + rowCount + "> entries.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void compareContent(final IDataSet currentDataSet, final IDataSet expectedDataSet, final AssertionErrorCollector errorCollector)
            throws DatabaseUnitException, ReflectiveOperationException {
        final String[] expectedTableNames = expectedDataSet.getTableNames();
        final FilteredDataSet filteredCurrentDataSet = new FilteredDataSet(new IncludeTableFilter(expectedTableNames), currentDataSet);

        for (final String tableName : expectedTableNames) {
            try {
                final List<String> columnsForSorting = defineColumnsForSorting(filteredCurrentDataSet, expectedDataSet, tableName);

                final ITable table = filteredCurrentDataSet.getTable(tableName);
                final ITable expectedTable = sort(new TableWrapper(expectedDataSet.getTable(tableName), table.getTableMetaData()),
                        columnsForSorting);
                final ITable currentTable = sort(table, columnsForSorting);

                final List<String> columnsToIgnore = extractColumnsToBeIgnored(expectedDataSet.getTable(tableName), table);

                final String[] toBeIgnored = columnsToIgnore.toArray(new String[columnsToIgnore.size()]);

                final ITable expectedTableWithFilteredColumns = filter(expectedTable, toBeIgnored);
                final ITable actualTableWithFilteredColumns = filter(currentTable, toBeIgnored);

                final DiffCollectingFailureHandler diffCollector = new DiffCollectingFailureHandler();
                Assertion.assertEquals(expectedTableWithFilteredColumns, actualTableWithFilteredColumns, diffCollector);

                collectErrors(errorCollector, diffCollector.getDiffList());
            } catch (final NoSuchTableException e) {
                final int rowCount = expectedDataSet.getTable(tableName).getRowCount();
                errorCollector.collect(
                        "Table " + tableName + " was expected to be present and to contain <" + rowCount + "> entries, but not found.");
            }
        }

        final List<String> currentTableNames = new ArrayList<>(Arrays.asList(currentDataSet.getTableNames()));
        currentTableNames.removeAll(Arrays.asList(expectedTableNames));
        for (final String notExpectedTableName : currentTableNames) {
            final int rowCount = currentDataSet.getTable(notExpectedTableName).getRowCount();
            errorCollector.collect(
                    "Table " + notExpectedTableName + " was not expected, but is present and contains <" + rowCount + "> entries.");
        }
    }

    private List<String> defineColumnsForSorting(final IDataSet currentDataSet, final IDataSet expectedDataSet, final String tableName)
            throws DataSetException {

        final List<String> additionalColumns = additionalColumnsForSorting(expectedDataSet.getTable(tableName),
                currentDataSet.getTable(tableName));

        final List<String> result = new ArrayList<>();
        result.addAll(orderBy.getColumns(tableName));
        result.addAll(additionalColumns);
        return result;
    }

    private ITable sort(final ITable table, final List<String> columnsForSorting) throws DataSetException {
        final SortedTable sortedTable = new SortedTable(table, columnsForSorting.toArray(new String[columnsForSorting.size()]));
        sortedTable.setUseComparable(true);
        return sortedTable;
    }

    private List<String> extractColumnsToBeIgnored(final ITable expectedTableState, final ITable currentTableState)
            throws DataSetException {
        final List<String> columnsToIgnore = extractNotExpectedColumnNames(expectedTableState, currentTableState);
        final String tableName = expectedTableState.getTableMetaData().getTableName();

        columnsToIgnore.addAll(toExclude.getColumns(tableName));

        final List<String> nonExistingColumns = new ArrayList<>(columnsToIgnore);
        nonExistingColumns.removeAll(extractColumnNames(currentTableState.getTableMetaData().getColumns()));

        if (!nonExistingColumns.isEmpty()) {
            log.warning("Columns which are specified to be filtered out " + Arrays.toString(nonExistingColumns.toArray())
                    + " are not existing in the table " + tableName);
        }
        return columnsToIgnore;
    }

    private ITable filter(final ITable table, final String[] columnsToFilter) throws ReflectiveOperationException, DataSetException {
        final ITable filteredTable = DefaultColumnFilter.excludedColumnsTable(table, columnsToFilter);
        return applyCustomFilters(filteredTable);
    }

    private void collectErrors(final AssertionErrorCollector errorCollector, final List<Difference> diffs) {
        for (final Difference diff : diffs) {
            final String tableName = diff.getActualTable().getTableMetaData().getTableName();
            errorCollector.collect(String.format(DIFF_ERROR, tableName, diff.getRowIndex(), diff.getColumnName(), diff.getExpectedValue(),
                    diff.getActualValue()));
        }
    }

    private List<String> additionalColumnsForSorting(final ITable expectedTableState, final ITable currentTableState)
            throws DataSetException {
        final List<String> columnsForSorting = new ArrayList<>();
        final Set<String> allColumns = new HashSet<>(extractColumnNames(expectedTableState.getTableMetaData().getColumns()));
        final Set<String> columnsToIgnore = new HashSet<>(extractColumnsToBeIgnored(expectedTableState, currentTableState));
        for (final String column : allColumns) {
            if (!columnsToIgnore.contains(column)) {
                columnsForSorting.add(column);
            }
        }

        return columnsForSorting;
    }

    private ITable applyCustomFilters(final ITable table) throws ReflectiveOperationException, DataSetException {
        ITable compositeTable = table;
        for (final Class<? extends IColumnFilter> columnFilter : columnFilters) {
            final IColumnFilter customColumnFilter = columnFilter.newInstance();
            final FilteredTableMetaData metaData = new FilteredTableMetaData(compositeTable.getTableMetaData(), customColumnFilter);
            compositeTable = new CompositeTable(metaData, compositeTable);
        }
        return compositeTable;
    }

    private Collection<String> extractColumnNames(final Column[] columns) {
        final List<String> names = new ArrayList<>(columns.length);
        for (final Column column : columns) {
            names.add(column.getColumnName().toLowerCase());
        }
        return names;
    }

    private List<String> extractNotExpectedColumnNames(final ITable expectedTable, final ITable currentTable) throws DataSetException {
        final Set<String> actualColumnNames = new HashSet<>();
        final Set<String> expectedColumnNames = new HashSet<>();

        if (currentTable != null) {
            actualColumnNames.addAll(extractColumnNames(currentTable.getTableMetaData().getColumns()));
        }

        if (expectedTable != null) {
            expectedColumnNames.addAll(extractColumnNames(expectedTable.getTableMetaData().getColumns()));
        }

        actualColumnNames.removeAll(expectedColumnNames);
        return new ArrayList<>(actualColumnNames);
    }

}
