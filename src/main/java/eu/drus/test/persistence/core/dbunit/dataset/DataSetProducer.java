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

        final List<Table> tables = createTables(dataset);

        for (final Table table : tables) {
            final ITableMetaData tableMetaData = createTableMetaData(table);
            consumer.startTable(tableMetaData);
            for (final Row row : table.getRows()) {
                final List<String> values = new ArrayList<String>();
                for (final Column column : tableMetaData.getColumns()) {
                    values.add(row.valueOf(column.getColumnName()));
                }
                consumer.row(values.toArray());
            }

            consumer.endTable();
        }

        consumer.endDataSet();
    }

    private ITableMetaData createTableMetaData(final Table table) {
        return new DefaultTableMetaData(table.getTableName(), createColumns(table.getColumns()));
    }

    private List<Table> createTables(final Map<String, List<Map<String, String>>> jsonStructure) {
        final List<Table> tables = new ArrayList<Table>();
        for (final Map.Entry<String, List<Map<String, String>>> entry : jsonStructure.entrySet()) {
            final Table table = new Table(entry.getKey());
            table.addColumns(extractColumns(entry.getValue()));
            table.addRows(extractRows(entry.getValue()));
            tables.add(table);
        }
        return tables;
    }

    private Column[] createColumns(final Collection<String> columnNames) {
        final List<Column> columns = columnNames.stream().map(e -> new Column(e, DataType.UNKNOWN)).collect(toList());
        return columns.toArray(new Column[columns.size()]);
    }

    private Collection<Row> extractRows(final List<Map<String, String>> rows) {
        return rows.stream().map(e -> new Row(e)).collect(toList());
    }

    private Collection<String> extractColumns(final List<Map<String, String>> rows) {
        return rows.stream().flatMap(e -> e.keySet().stream()).collect(toSet());
    }

}
