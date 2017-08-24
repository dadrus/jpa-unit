package eu.drus.jpa.unit.cassandra;

import eu.drus.jpa.unit.api.DataSeedStrategy.StrategyProvider;
import eu.drus.jpa.unit.cassandra.operation.CassandraOperation;
import eu.drus.jpa.unit.cassandra.operation.CassandraOperations;

public class DataSeedStrategyProvider implements StrategyProvider<CassandraOperation> {

    @Override
    public CassandraOperation insertStrategy() {
        return CassandraOperations.INSERT;
    }

    @Override
    public CassandraOperation cleanInsertStrategy() {
        return CassandraOperations.CLEAN_INSERT;
    }

    @Override
    public CassandraOperation refreshStrategy() {
        return CassandraOperations.REFRESH;
    }

    @Override
    public CassandraOperation updateStrategy() {
        return CassandraOperations.UPDATE;
    }

}
