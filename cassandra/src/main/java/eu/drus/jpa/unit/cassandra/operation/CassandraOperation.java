package eu.drus.jpa.unit.cassandra.operation;

import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.cassandra.dataset.DataSet;

public interface CassandraOperation {

    default String quote(final String columnName) {
        final StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append(columnName);
        sb.append('"');
        return sb.toString();
    }

    void execute(Session session, DataSet data);
}
