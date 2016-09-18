package eu.drus.test.persistence.core.dbunit;

import static eu.drus.test.persistence.core.dbunit.DataSetUtils.extractColumnNames;
import static eu.drus.test.persistence.core.dbunit.DataSetUtils.extractColumnsNotSpecifiedInExpectedDataSet;
import static eu.drus.test.persistence.core.dbunit.DataSetUtils.extractNonExistingColumns;

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
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.FilteredTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.IncludeTableFilter;

import eu.drus.test.persistence.core.AssertionErrorCollector;
import eu.drus.test.persistence.core.dbunit.dataset.TableWrapper;

public class DataSetComparator {

    private static final String DIFF_ERROR = "%s | In row %d: expected value of %s \"%s\" but was \"%s\".";

    private static final Logger log = Logger.getLogger(DataSetComparator.class.getName());

    final ColumnsHolder toExclude;

    final ColumnsHolder orderBy;

    final Set<Class<? extends IColumnFilter>> columnFilters;

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

    public void compareContent(final IDataSet currentDataSet, final IDataSet expectedDataSet, final AssertionErrorCollector errorCollector)
            throws DatabaseUnitException, ReflectiveOperationException {
        final String[] tableNames = expectedDataSet.getTableNames();
        final FilteredDataSet filteredCurrentDataSet = new FilteredDataSet(new IncludeTableFilter(tableNames), currentDataSet);

        for (final String tableName : tableNames) {
            final List<String> columnsForSorting = defineColumnsForSorting(filteredCurrentDataSet, expectedDataSet, tableName);

            final ITable expectedTable = sort(
                    new TableWrapper(expectedDataSet.getTable(tableName), filteredCurrentDataSet.getTable(tableName).getTableMetaData()),
                    columnsForSorting);
            final ITable currentTable = sort(filteredCurrentDataSet.getTable(tableName), columnsForSorting);

            final List<String> columnsToIgnore = extractColumnsToBeIgnored(expectedDataSet.getTable(tableName),
                    filteredCurrentDataSet.getTable(tableName));

            final DiffCollectingFailureHandler diffCollector = new DiffCollectingFailureHandler();

            final ITable expectedTableWithFilteredColumns = filter(expectedTable, toStringArray(columnsToIgnore));
            final ITable actualTableWithFilteredColumns = filter(currentTable, toStringArray(columnsToIgnore));

            Assertion.assertEquals(expectedTableWithFilteredColumns, actualTableWithFilteredColumns, diffCollector);

            @SuppressWarnings("unchecked")
            final List<Difference> diffs = diffCollector.getDiffList();
            collectErrors(errorCollector, diffs);
        }
    }

    public void shouldBeEmpty(final IDataSet dataSet, final AssertionErrorCollector errorCollector) throws DatabaseUnitException {
        final String[] tableNames = dataSet.getTableNames();
        for (final String tableName : tableNames) {
            shouldBeEmpty(dataSet, tableName, errorCollector);
        }
    }

    public void shouldBeEmpty(final IDataSet dataSet, final String tableName, final AssertionErrorCollector errorCollector)
            throws DataSetException {
        final SortedTable tableState = new SortedTable(dataSet.getTable(tableName));
        final int rowCount = tableState.getRowCount();
        if (rowCount != 0) {
            errorCollector.collect(new AssertionError(tableName + " expected to be empty, but was <" + rowCount + ">."));
        }
    }

    // -- Private methods

    private void collectErrors(final AssertionErrorCollector errorCollector, final List<Difference> diffs) {
        for (final Difference diff : diffs) {
            final String tableName = diff.getActualTable().getTableMetaData().getTableName();
            errorCollector.collect(String.format(DIFF_ERROR, tableName, diff.getRowIndex(), diff.getColumnName(), diff.getExpectedValue(),
                    diff.getActualValue()));
        }
    }

    private ITable sort(final ITable table, final List<String> columnsForSorting) throws DataSetException {
        final SortedTable sortedTable = new SortedTable(table, toStringArray(columnsForSorting));
        sortedTable.setUseComparable(true);
        return sortedTable;
    }

    private List<String> defineColumnsForSorting(final IDataSet currentDataSet, final IDataSet expectedDataSet, final String tableName)
            throws DataSetException {
        final List<String> columnsForSorting = new ArrayList<String>();
        columnsForSorting.addAll(orderBy.global);
        final List<String> columsPerTable = orderBy.columnsPerTable.get(tableName);
        if (columsPerTable != null) {
            columnsForSorting.addAll(columsPerTable);
        }
        columnsForSorting.addAll(additionalColumnsForSorting(expectedDataSet.getTable(tableName), currentDataSet.getTable(tableName)));
        return columnsForSorting;
    }

    private static <T> String[] toStringArray(final Collection<T> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    private List<String> additionalColumnsForSorting(final ITable expectedTableState, final ITable currentTableState)
            throws DataSetException {
        final List<String> columnsForSorting = new ArrayList<String>();
        final Set<String> allColumns = new HashSet<String>(extractColumnNames(expectedTableState.getTableMetaData().getColumns()));
        final Set<String> columnsToIgnore = new HashSet<String>(extractColumnsToBeIgnored(expectedTableState, currentTableState));
        for (final String column : allColumns) {
            if (!columnsToIgnore.contains(column)) {
                columnsForSorting.add(column);
            }
        }

        return columnsForSorting;
    }

    private List<String> extractColumnsToBeIgnored(final ITable expectedTableState, final ITable currentTableState)
            throws DataSetException {
        final List<String> columnsToIgnore = extractColumnsNotSpecifiedInExpectedDataSet(expectedTableState, currentTableState);
        final String tableName = expectedTableState.getTableMetaData().getTableName();
        final List<String> tableColumns = toExclude.columnsPerTable.get(tableName);

        columnsToIgnore.addAll(toExclude.global);

        if (tableColumns != null) {
            columnsToIgnore.addAll(tableColumns);
        }

        final List<String> nonExistingColumns = extractNonExistingColumns(columnsToIgnore,
                extractColumnNames(currentTableState.getTableMetaData().getColumns()));

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

    private ITable applyCustomFilters(ITable table) throws ReflectiveOperationException, DataSetException {
        for (final Class<? extends IColumnFilter> columnFilter : columnFilters) {
            final IColumnFilter customColumnFilter = columnFilter.newInstance();
            final FilteredTableMetaData metaData = new FilteredTableMetaData(table.getTableMetaData(), customColumnFilter);
            table = new CompositeTable(metaData, table);
        }
        return table;
    }

}
