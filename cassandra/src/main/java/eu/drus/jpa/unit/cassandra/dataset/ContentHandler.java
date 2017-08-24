package eu.drus.jpa.unit.cassandra.dataset;

import java.util.List;

public interface ContentHandler {

    void onDataSetStart();

    void onDataSetEnd();

    void onTableStart(TableProperties properties);

    void onTableEnd();

    void onRow(List<RowElement> row);
}
