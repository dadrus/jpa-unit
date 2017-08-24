package eu.drus.jpa.unit.cassandra.operation;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;

import eu.drus.jpa.unit.cassandra.dataset.DataSet;
import eu.drus.jpa.unit.cassandra.dataset.RowElement;
import eu.drus.jpa.unit.cassandra.dataset.Table;

public class InsertOperation implements CassandraOperation {

    @Override
    public void execute(final Session session, final DataSet data) {
        final String keyspace = session.getLoggedKeyspace();

        final List<RegularStatement> stmtList = new ArrayList<>();
        for (final Table table : data) {
            for (final List<RowElement> row : table) {
                final Insert insertStmt = insertInto(quote(keyspace), quote(table.getTableProperties().getTableName()));
                row.forEach(el -> insertStmt.value(quote(el.getColumn().getName()), el.getValue()));
                stmtList.add(insertStmt);
            }
        }

        session.execute(batch(stmtList.toArray(new RegularStatement[stmtList.size()])));
    }

}
