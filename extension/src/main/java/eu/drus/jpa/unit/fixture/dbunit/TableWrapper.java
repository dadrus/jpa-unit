package eu.drus.jpa.unit.fixture.dbunit;

import org.dbunit.dataset.AbstractTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

public class TableWrapper extends AbstractTable {

    private ITable table;
    private ITableMetaData metaData;

    public TableWrapper(final ITable table, final ITableMetaData metaData) {
        this.table = table;
        this.metaData = metaData;
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return metaData;
    }

    @Override
    public int getRowCount() {
        return table.getRowCount();
    }

    @Override
    public Object getValue(final int row, final String column) throws DataSetException {
        return table.getValue(row, column);
    }
}
