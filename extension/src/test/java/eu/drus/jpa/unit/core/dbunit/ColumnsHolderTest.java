package eu.drus.jpa.unit.core.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import eu.drus.jpa.unit.core.dbunit.ColumnsHolder;

public class ColumnsHolderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateColmnHolderForAnEmptyColumnName() {
        new ColumnsHolder(new String[] {
                ""
        });
    }

    @Test
    public void testCreateColumnHolderForColumnNamesWithoutTableNamesOnly() {
        // GIVEN
        final String column1 = "col1";
        final String column2 = "col2";

        // WHEN
        final ColumnsHolder columnsHolder = new ColumnsHolder(new String[] {
                column1, column2
        });

        // THEN
        final List<String> columns = columnsHolder.getColumns("foo");

        assertThat(columns, notNullValue());
        assertThat(columns.size(), equalTo(2));
        assertThat(columns, hasItem(column1));
        assertThat(columns, hasItem(column2));
    }

    @Test
    public void testCreateColumnHolderForColumnNamesWithTableNamesOnly() {
        // GIVEN
        final String column1 = "tab.col1";
        final String column2 = "tab.col2";

        // WHEN
        final ColumnsHolder columnsHolder = new ColumnsHolder(new String[] {
                column1, column2
        });

        // THEN
        List<String> columns = columnsHolder.getColumns("tab");

        assertThat(columns, notNullValue());
        assertThat(columns.size(), equalTo(2));
        assertThat(columns, hasItem(column1.substring(column1.indexOf('.') + 1)));
        assertThat(columns, hasItem(column2.substring(column2.indexOf('.') + 1)));

        columns = columnsHolder.getColumns("foo");
        assertThat(columns, notNullValue());
        assertThat(columns.size(), equalTo(0));
    }

    @Test
    public void testCreateColumnHolderForMixModeColumnNames() {
        // GIVEN
        final String column1 = "tab.col1";
        final String column2 = "tab.col2";
        final String column3 = "col3";

        // WHEN
        final ColumnsHolder columnsHolder = new ColumnsHolder(new String[] {
                column1, column2, column3
        });

        // THEN
        List<String> columns = columnsHolder.getColumns("tab");

        assertThat(columns, notNullValue());
        assertThat(columns.size(), equalTo(3));
        assertThat(columns, hasItem(column1.substring(column1.indexOf('.') + 1)));
        assertThat(columns, hasItem(column2.substring(column2.indexOf('.') + 1)));
        assertThat(columns, hasItem(column3));

        columns = columnsHolder.getColumns("foo");
        assertThat(columns, notNullValue());
        assertThat(columns.size(), equalTo(1));
        assertThat(columns, hasItem(column3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateColumnHolderProvidingInvalidColumnName() {
        // GIVEN
        final String column1 = "tab.tab.col1";

        // WHEN
        new ColumnsHolder(new String[] {
                column1
        });

        // THEN
        // exception is thrown
    }
}
