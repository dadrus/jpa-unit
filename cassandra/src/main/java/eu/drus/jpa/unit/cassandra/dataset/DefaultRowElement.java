package eu.drus.jpa.unit.cassandra.dataset;

public class DefaultRowElement implements RowElement {

    private Column column;
    private Object value;

    public DefaultRowElement(final Column column, final Object value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public Object getValue() {
        return value;
    }

}
