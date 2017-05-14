package eu.drus.jpa.unit.mongodb;

import eu.drus.jpa.unit.api.DataSeedStrategy.StrategyProvider;

public class DataSeedStrategyProvider implements StrategyProvider<AbstractDbOperation> {

    @Override
    public AbstractDbOperation insertStrategy() {
        return AbstractDbOperation.INSERT;
    }

    @Override
    public AbstractDbOperation cleanInsertStrategy() {
        return AbstractDbOperation.CLEAN_INSERT;
    }

    @Override
    public AbstractDbOperation refreshStrategy() {
        return AbstractDbOperation.REFRESH;
    }

    @Override
    public AbstractDbOperation updateStrategy() {
        return AbstractDbOperation.UPDATE;
    }

}
