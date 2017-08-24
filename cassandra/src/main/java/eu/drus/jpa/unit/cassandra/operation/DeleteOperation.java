package eu.drus.jpa.unit.cassandra.operation;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;

import eu.drus.jpa.unit.cassandra.dataset.DataSet;
import eu.drus.jpa.unit.cassandra.dataset.RowElement;

public class DeleteOperation implements CassandraOperation {

    @Override
    public void execute(final Session session, final DataSet data) {
        final String keySpace = session.getLoggedKeyspace();

        data.forEach(t -> {

            final List<String> partitionKeyColumnNames = getNamesOfPartitionKeyColumns(session, keySpace,
                    t.getTableProperties().getTableName());

            t.forEach(row -> {
                final Delete deleteFrom = delete().from(quote(keySpace), quote(t.getTableProperties().getTableName()));

                partitionKeyColumnNames.forEach(name -> {
                    // find key value
                    final RowElement entry = row.stream().filter(e -> e.getColumn().getName().equals(name)).findFirst().get();
                    deleteFrom.where(eq(name, entry.getValue()));
                });
                session.execute(deleteFrom);
            });

        });
    }

    private List<String> getNamesOfPartitionKeyColumns(final Session session, final String keySpace, final String tableName) {
        final ResultSet columnDefsRs = session.execute(select().column("column_name").column("kind").from("system_schema", "columns")
                .where(eq("keyspace_name", keySpace)).and(eq("table_name", tableName)));

        final List<String> partitionKeyColumnNames = new ArrayList<>();
        final Iterator<Row> it = columnDefsRs.iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final String columnKind = row.get("kind", String.class);
            if (columnKind.equals("partition_key")) {
                partitionKeyColumnNames.add(row.get("column_name", String.class));
            }
        }
        return partitionKeyColumnNames;
    }
}
