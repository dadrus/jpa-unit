package eu.drus.jpa.unit.cassandra.dataset;

import java.util.List;

public interface Table extends Iterable<List<RowElement>> {

    TableProperties getTableProperties();

    int getRowCount();

    List<RowElement> getRow(int index);

}
