package eu.drus.test.persistence.core.dbunit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IColumnFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.JpaTestException;
import eu.drus.test.persistence.core.AssertionErrorCollector;

@RunWith(MockitoJUnitRunner.class)
public class DataSetComparatorTest {

    private static final String TABLE_1_NAME = "TABLE_1";
    private static final int TABLE_1_ENTRIES = 2;
    private static final Column TABLE_1_COLUMN_1 = new Column("t1col1", DataType.VARCHAR);
    private static final Column TABLE_1_COLUMN_2 = new Column("t1col2", DataType.VARCHAR);

    private static final String TABLE_2_NAME = "TABLE_2";
    private static final int TABLE_2_ENTRIES = 3;
    private static final Column TABLE_2_COLUMN_1 = new Column("t2col1", DataType.VARCHAR);
    private static final Column TABLE_2_COLUMN_2 = new Column("t2col2", DataType.VARCHAR);
    private static final Column TABLE_2_COLUMN_3 = new Column("t2col3", DataType.VARCHAR);

    private static final String TABLE_3_NAME = "TABLE_3";
    private static final int TABLE_3_ENTRIES = 4;
    private static final Column TABLE_3_COLUMN_1 = new Column("t3col1", DataType.VARCHAR);
    private static final Column TABLE_3_COLUMN_2 = new Column("t3col2", DataType.VARCHAR);
    private static final Column TABLE_3_COLUMN_3 = new Column("t3col3", DataType.VARCHAR);
    private static final Column TABLE_3_COLUMN_4 = new Column("t3col4", DataType.VARCHAR);

    private static final Column TABLE_4_COLUMN_1 = new Column("t1col1", DataType.DOUBLE);
    private static final Column TABLE_4_COLUMN_2 = new Column("t1col2", DataType.VARCHAR);

    @Mock
    private IDataSet currentDataSet;

    @Mock
    private IDataSet expectedDataSet;

    @Mock
    private ITable table1;

    @Mock
    private ITable table2;

    @Mock
    private ITable table3;

    @Mock
    private ITable table4;

    @Mock
    private ITableMetaData table1MetaData;

    @Mock
    private ITableMetaData table2MetaData;

    @Mock
    private ITableMetaData table3MetaData;

    @Mock
    private ITableMetaData table4MetaData;

    @Before
    public void prepareMocks() throws DataSetException {
        when(table1.getRowCount()).thenReturn(TABLE_1_ENTRIES);
        when(table1.getTableMetaData()).thenReturn(table1MetaData);
        when(table1MetaData.getTableName()).thenReturn(TABLE_1_NAME);
        when(table1MetaData.getColumns()).thenReturn(new Column[] {
                TABLE_1_COLUMN_1, TABLE_1_COLUMN_2
        });
        when(table1MetaData.getColumnIndex(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String colName = (String) invocation.getArguments()[0];
            if (colName == TABLE_1_COLUMN_1.getColumnName()) {
                return 0;
            } else if (colName == TABLE_1_COLUMN_2.getColumnName()) {
                return 1;
            } else {
                throw new NoSuchColumnException(TABLE_1_NAME, colName, "No such column");
            }
        });

        when(table2.getRowCount()).thenReturn(TABLE_2_ENTRIES);
        when(table2.getTableMetaData()).thenReturn(table2MetaData);
        when(table2MetaData.getTableName()).thenReturn(TABLE_2_NAME);
        when(table2MetaData.getColumns()).thenReturn(new Column[] {
                TABLE_2_COLUMN_1, TABLE_2_COLUMN_2, TABLE_2_COLUMN_3
        });
        when(table2MetaData.getColumnIndex(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String colName = (String) invocation.getArguments()[0];
            if (colName == TABLE_2_COLUMN_1.getColumnName()) {
                return 0;
            } else if (colName == TABLE_2_COLUMN_2.getColumnName()) {
                return 1;
            } else if (colName == TABLE_2_COLUMN_3.getColumnName()) {
                return 2;
            } else {
                throw new NoSuchColumnException(TABLE_2_NAME, colName, "No such column");
            }
        });

        when(table3.getRowCount()).thenReturn(TABLE_3_ENTRIES);
        when(table3.getTableMetaData()).thenReturn(table3MetaData);
        when(table3MetaData.getTableName()).thenReturn(TABLE_3_NAME);
        when(table3MetaData.getColumns()).thenReturn(new Column[] {
                TABLE_3_COLUMN_1, TABLE_3_COLUMN_2, TABLE_3_COLUMN_3, TABLE_3_COLUMN_4
        });
        when(table3MetaData.getColumnIndex(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String colName = (String) invocation.getArguments()[0];
            if (colName == TABLE_3_COLUMN_1.getColumnName()) {
                return 0;
            } else if (colName == TABLE_3_COLUMN_2.getColumnName()) {
                return 1;
            } else if (colName == TABLE_3_COLUMN_3.getColumnName()) {
                return 2;
            } else if (colName == TABLE_3_COLUMN_4.getColumnName()) {
                return 3;
            } else {
                throw new NoSuchColumnException(TABLE_3_NAME, colName, "No such column");
            }
        });

        when(table4.getRowCount()).thenReturn(TABLE_1_ENTRIES);
        when(table4.getTableMetaData()).thenReturn(table4MetaData);
        when(table4MetaData.getTableName()).thenReturn(TABLE_1_NAME);
        when(table4MetaData.getColumns()).thenReturn(new Column[] {
                TABLE_4_COLUMN_1, TABLE_4_COLUMN_2
        });
        when(table4MetaData.getColumnIndex(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String colName = (String) invocation.getArguments()[0];
            if (colName == TABLE_4_COLUMN_1.getColumnName()) {
                return 0;
            } else if (colName == TABLE_4_COLUMN_2.getColumnName()) {
                return 1;
            } else {
                throw new NoSuchColumnException(TABLE_1_NAME, colName, "No such column");
            }
        });
    }

    @Test
    public void testCurrentDataSetContainsDataAndExpectedDataSetIsEmpty() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {});
        when(expectedDataSet.getTable(any(String.class))).thenThrow(new NoSuchTableException());

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(),
                    containsString(TABLE_1_NAME + " was expected to be empty, but has <" + TABLE_1_ENTRIES + "> entries"));
        }
    }

    @Test
    public void testCurrentDataSetIsEmptyAndExpectedDataContainsData() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(currentDataSet.getTableNames()).thenReturn(new String[] {});
        when(currentDataSet.getTable(any(String.class))).thenThrow(new NoSuchTableException());

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(), containsString(
                    TABLE_1_NAME + " was expected to be present and to contain <" + TABLE_1_ENTRIES + "> entries, but not found"));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetContainDataButAreFullyDisjunctiveUsingNotStrictMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetContainDataButAreFullyDisjunctiveUsingStrictMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, true, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 case"));
            assertThat(e.getMessage(), containsString(
                    TABLE_1_NAME + " was expected to be present and to contain <" + TABLE_1_ENTRIES + "> entries, but not found"));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetHaveOnlyOneTableInCommonUsingNotStrictMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME, TABLE_2_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME, TABLE_3_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else if (tableName == TABLE_3_NAME) {
                return table3;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(), containsString(
                    TABLE_1_NAME + " was expected to be present and to contain <" + TABLE_1_ENTRIES + "> entries, but not found"));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetHaveOnlyOneTableInCommonUsingStrictMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, true, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME, TABLE_2_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME, TABLE_3_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else if (tableName == TABLE_3_NAME) {
                return table3;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 case"));
            assertThat(e.getMessage(), containsString(
                    TABLE_1_NAME + " was expected to be present and to contain <" + TABLE_1_ENTRIES + "> entries, but not found"));
            assertThat(e.getMessage(),
                    containsString(TABLE_3_NAME + " was not expected, but is present and contains <" + TABLE_3_ENTRIES + "> entries"));
        }
    }

    @Test
    public void testCurrentDataSetIsASubsetOfExpectedDataSet() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME, TABLE_2_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(),
                    containsString(TABLE_1_NAME + " was expected to be present and to contain <" + TABLE_1_ENTRIES + "> entries"));
        }
    }

    @Test
    public void testExpectedDataSetIsASubsetOfCurrentDataSetUsingNotStringMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME, TABLE_2_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(0));

        errorCollector.report();
    }

    @Test
    public void testExpectedDataSetIsASubsetOfCurrentDataSetUsingStringMode() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, true, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_2_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME, TABLE_2_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else if (tableName == TABLE_2_NAME) {
                return table2;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(),
                    containsString(TABLE_1_NAME + " was not expected, but is present and contains <" + TABLE_1_ENTRIES + "> entries"));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetDifferInTableRecords() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table4;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_other1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_other2");

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 cases"));
            assertThat(e.getMessage(), containsString("\"col2_value1\" but was \"col2_other1\""));
            assertThat(e.getMessage(), containsString("\"col2_value2\" but was \"col2_other2\""));
        }
    }

    @Test
    public void testCurrentDataSetAndExpectedDataSetAreEqual() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = Collections.emptySet();
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table4;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table4.getRowCount()).thenReturn(TABLE_1_ENTRIES);
        when(table4.getTableMetaData()).thenReturn(table1MetaData);
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(0));

        errorCollector.report();
    }

    @Test
    public void testCustomFilter() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = new HashSet<>(Arrays.asList(MyCustomFilter.class));
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table4;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table4.getRowCount()).thenReturn(TABLE_1_ENTRIES);
        when(table4.getTableMetaData()).thenReturn(table1MetaData);
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        // WHEN
        comparator.compare(currentDataSet, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(0));

        errorCollector.report();
    }

    @Test
    public void testCustomFilterImplementedNotAccordingToTheRequirements() throws Exception {
        // GIVEN
        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        final String[] orderBy = new String[] {};
        final String[] toExclude = new String[] {};
        final Set<Class<? extends IColumnFilter>> columnFilters = new HashSet<>(Arrays.asList(BadCustomFilter.class));
        final DataSetComparator comparator = new DataSetComparator(orderBy, toExclude, false, columnFilters);

        when(expectedDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(expectedDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table1;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table1.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table1.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        when(currentDataSet.getTableNames()).thenReturn(new String[] {
                TABLE_1_NAME
        });
        when(currentDataSet.getTable(any(String.class))).thenAnswer((final InvocationOnMock invocation) -> {
            final String tableName = (String) invocation.getArguments()[0];
            if (tableName == TABLE_1_NAME) {
                return table4;
            } else {
                throw new NoSuchTableException(tableName);
            }
        });
        when(table4.getRowCount()).thenReturn(TABLE_1_ENTRIES);
        when(table4.getTableMetaData()).thenReturn(table1MetaData);
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_1.getColumnName()))).thenReturn("col1_value2");
        when(table4.getValue(eq(0), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value1");
        when(table4.getValue(eq(1), eq(TABLE_1_COLUMN_2.getColumnName()))).thenReturn("col2_value2");

        // WHEN
        try {
            comparator.compare(currentDataSet, expectedDataSet, errorCollector);
            fail("JpaTestException expected");
        } catch (final JpaTestException e) {
            assertThat(e.getMessage(), containsString("Could not instanciate"));
        }
    }

    public static class MyCustomFilter implements IColumnFilter {

        @Override
        public boolean accept(final String tableName, final Column column) {
            // reject column 1
            return column.getColumnName() != TABLE_1_COLUMN_1.getColumnName();
        }
    }

    public static class BadCustomFilter implements IColumnFilter {

        public BadCustomFilter(final boolean val) {

        }

        @Override
        public boolean accept(final String tableName, final Column column) {
            return false;
        }
    }
}
