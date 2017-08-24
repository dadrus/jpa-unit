package eu.drus.jpa.unit.cassandra.dataset;

public interface RowElement {

    Column getColumn();

    Object getValue();
}
