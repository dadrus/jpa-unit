package eu.drus.jpa.unit.cassandra.operation;

import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.cassandra.dataset.DataSet;

public class CompositeOperation implements CassandraOperation {

    private CassandraOperation[] operations;

    public CompositeOperation(final CassandraOperation... operations) {
        this.operations = operations;
    }

    @Override
    public void execute(final Session connection, final DataSet data) {
        for (final CassandraOperation operation : operations) {
            operation.execute(connection, data);
        }
    }

}
