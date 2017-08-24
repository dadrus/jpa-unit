package eu.drus.jpa.unit.cassandra.operation;

import static com.datastax.driver.core.querybuilder.QueryBuilder.truncate;

import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.cassandra.dataset.DataSet;

public class DeleteAllOperation implements CassandraOperation {

    @Override
    public void execute(final Session session, final DataSet data) {
        final String keySpace = session.getLoggedKeyspace();

        data.forEach(t -> session.execute(truncate(quote(keySpace), quote(t.getTableProperties().getTableName()))));
    }

}
