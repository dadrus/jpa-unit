package eu.drus.jpa.unit.cassandra.dataset;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.cassandra.dataset.PrimaryKey.Builder;

public class DataBaseDataSet implements DataSet {

    private Map<String, Table> tables = new HashMap<>();

    public DataBaseDataSet(final Session session) {
        readContents(session);
    }

    private void readContents(final Session session) {
        final String keySpace = session.getLoggedKeyspace();

        // get tables from the used key space
        final ResultSet tableNamesRs = session
                .execute(select().column("table_name").from("system_schema", "tables").where(eq("keyspace_name", keySpace)));

        // read table definitions
        tableNamesRs.forEach(tableNameRow -> {
            final String tableName = tableNameRow.get("table_name", String.class);
            final ResultSet columnDefsRs = session.execute(select().column("column_name").column("kind").column("type")
                    .from("system_schema", "columns").where(eq("keyspace_name", keySpace)).and(eq("table_name", tableName)));

            // read column definitions
            final Builder pkBuilder = PrimaryKey.builder();
            final Map<String, Column> columns = new HashMap<>();
            columnDefsRs.forEach(columnRow -> {
                final String columnName = columnRow.get("column_name", String.class);
                final String kind = columnRow.get("kind", String.class);
                final String type = columnRow.get("type", String.class);

                final Column column = new Column(columnName, ColumnType.fromString(type));
                columns.put(columnName, column);
                if (kind.equalsIgnoreCase("partition_key")) {
                    pkBuilder.withPartitionKey(column);
                } else if (kind.equalsIgnoreCase("clustering_key")) {
                    pkBuilder.withClusteringKey(column);
                }
            });

            final TableProperties properties = new DefaultTableProperties(tableName, columns, pkBuilder.build());
            final List<List<RowElement>> tableData = new ArrayList<>();

            // read data
            final ResultSet dataRs = session.execute(select().all().from(quote(keySpace), quote(tableName)));
            dataRs.forEach(dataRow -> {
                final List<RowElement> row = new ArrayList<>();
                properties.getColumns().forEach(c -> {
                    final Object obj = dataRow.get(c.getName(), c.getType().getJavaClass());
                    row.add(new DefaultRowElement(c, obj));
                });
                tableData.add(row);
            });

            tables.put(tableName, new DefaultTable(properties, tableData));
        });

    }

    private String quote(final String columnName) {
        final StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append(columnName);
        sb.append('"');
        return sb.toString();
    }

    @Override
    public Iterator<Table> iterator() {
        return tables.values().iterator();
    }

    @Override
    public Optional<Table> getTable(final String name) {
        return Optional.ofNullable(tables.get(name));
    }

}
