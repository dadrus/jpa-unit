package eu.drus.test.persistence.core.dbunit.dataset;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;

public abstract class DataSetProducer implements IDataSetProducer {

    private IDataSetConsumer consumer = new DefaultConsumer();

    protected final InputStream input;

    public DataSetProducer(final InputStream input) {
        this.input = input;
    }

    protected abstract Map<String, List<Map<String, String>>> loadDataSet() throws DataSetException;

    @Override
    public void setConsumer(final IDataSetConsumer consumer) throws DataSetException {
        this.consumer = consumer;
    }

    @Override
    public void produce() throws DataSetException {

        consumer.startDataSet();

        final Map<String, List<Map<String, String>>> dataset = loadDataSet();

        for (final Map.Entry<String, List<Map<String, String>>> entry : dataset.entrySet()) {
            // an entry represents a table
            final List<Map<String, String>> rows = entry.getValue();
            final Collection<String> columnNames = extractColumnNames(rows);
            final ITableMetaData tableMetaData = new DefaultTableMetaData(entry.getKey(), createColumns(columnNames));

            consumer.startTable(tableMetaData);

            for (final Map<String, String> row : rows) {
                final List<String> values = new ArrayList<>();
                for (final Column column : tableMetaData.getColumns()) {
                    values.add(row.get(column.getColumnName()));
                }
                consumer.row(values.toArray());
            }

            consumer.endTable();

        }

        consumer.endDataSet();
    }

    private Column[] createColumns(final Collection<String> columnNames) {
        final List<Column> columns = columnNames.stream().map(e -> new Column(e, DataType.UNKNOWN)).collect(toList());
        return columns.toArray(new Column[columns.size()]);
    }

    private Collection<String> extractColumnNames(final List<Map<String, String>> rows) {
        return rows.stream().flatMap(e -> e.keySet().stream()).collect(toSet());
    }

}
