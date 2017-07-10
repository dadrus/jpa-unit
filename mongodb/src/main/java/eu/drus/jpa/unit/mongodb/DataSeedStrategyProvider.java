package eu.drus.jpa.unit.mongodb;

import eu.drus.jpa.unit.api.DataSeedStrategy.StrategyProvider;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperation;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperations;

public class DataSeedStrategyProvider implements StrategyProvider<MongoDbOperation> {

    @Override
    public MongoDbOperation insertStrategy() {
        return MongoDbOperations.INSERT;
    }

    @Override
    public MongoDbOperation cleanInsertStrategy() {
        return MongoDbOperations.CLEAN_INSERT;
    }

    @Override
    public MongoDbOperation refreshStrategy() {
        return MongoDbOperations.REFRESH;
    }

    @Override
    public MongoDbOperation updateStrategy() {
        return MongoDbOperations.UPDATE;
    }

}
