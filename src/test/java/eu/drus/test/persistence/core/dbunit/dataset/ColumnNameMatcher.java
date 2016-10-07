package eu.drus.test.persistence.core.dbunit.dataset;

import org.dbunit.dataset.Column;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

class ColumnNameMatcher extends BaseMatcher<Column> {

    public static ColumnNameMatcher columnWithName(final String name) {
        return new ColumnNameMatcher(name);
    }

    private String columnName;

    public ColumnNameMatcher(final String columnName) {
        this.columnName = columnName;
    }

    @Override
    public boolean matches(final Object item) {
        return ((Column) item).getColumnName().equals(columnName);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a column matching given name");
    }

}
