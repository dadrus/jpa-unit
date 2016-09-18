package eu.drus.test.persistence.core.dbunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.ExcludeTableFilter;

public final class DataSetUtils {

    private DataSetUtils() {}

    public static IDataSet mergeDataSets(final List<IDataSet> dataSets) throws DataSetException {
        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
    }

    public static IDataSet excludeTables(final IDataSet dataSet, final String... tablesToExclude) {
        return new FilteredDataSet(new ExcludeTableFilter(tablesToExclude), dataSet);
    }

    public static Collection<String> extractColumnNames(final Column[] columns) {
        final List<String> names = new ArrayList<String>(columns.length);
        for (final Column column : columns) {
            names.add(column.getColumnName().toLowerCase());
        }
        return names;
    }

    public static List<String> extractColumnsNotSpecifiedInExpectedDataSet(final ITable expectedTableState, final ITable currentTableState)
            throws DataSetException {
        final Set<String> allColumns = new HashSet<String>(extractColumnNames(currentTableState.getTableMetaData().getColumns()));
        final Set<String> expectedColumnNames = new HashSet<String>(extractColumnNames(expectedTableState.getTableMetaData().getColumns()));
        return extractNonExistingColumns(allColumns, expectedColumnNames);
    }

    public static List<String> extractNonExistingColumns(final Collection<String> expectedColumns, final Collection<String> actualColumns) {
        final List<String> columnsNotSpecifiedInExpectedDataSet = new ArrayList<String>();

        for (final String column : expectedColumns) {
            if (!actualColumns.contains(column.toLowerCase())) {
                columnsNotSpecifiedInExpectedDataSet.add(column.toLowerCase());
            }
        }

        return columnsNotSpecifiedInExpectedDataSet;
    }
}
