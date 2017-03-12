package eu.drus.jpa.unit.decorator.dbunit;

import org.dbunit.operation.DatabaseOperation;

import eu.drus.jpa.unit.api.DataSeedStrategy.StrategyProvider;

public class DataSeedStrategyProvider implements StrategyProvider<DatabaseOperation> {

    @Override
    public DatabaseOperation insertStrategy() {
        return DatabaseOperation.INSERT;
    }

    @Override
    public DatabaseOperation cleanInsertStrategy() {
        return DatabaseOperation.CLEAN_INSERT;
    }

    @Override
    public DatabaseOperation refreshStrategy() {
        return DatabaseOperation.REFRESH;
    }

    @Override
    public DatabaseOperation updateStrategy() {
        return DatabaseOperation.UPDATE;
    }

}
