package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.mongodb.operation.MongoDbOperation;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperations;

public class DataSeedStrategyProviderTest {

    private static final DataSeedStrategyProvider STRATEGY_PROVIDER = new DataSeedStrategyProvider();

    @Test
    public void testCleanInsertStrategy() {
        final MongoDbOperation operation = STRATEGY_PROVIDER.cleanInsertStrategy();

        assertThat(operation, equalTo(MongoDbOperations.CLEAN_INSERT));
    }

    @Test
    public void testInsertStrategy() {
        final MongoDbOperation operation = STRATEGY_PROVIDER.insertStrategy();

        assertThat(operation, equalTo(MongoDbOperations.INSERT));
    }

    @Test
    public void testRefreshStrategy() {
        final MongoDbOperation operation = STRATEGY_PROVIDER.refreshStrategy();

        assertThat(operation, equalTo(MongoDbOperations.REFRESH));
    }

    @Test
    public void testUpdateStrategy() {
        final MongoDbOperation operation = STRATEGY_PROVIDER.updateStrategy();

        assertThat(operation, equalTo(MongoDbOperations.UPDATE));
    }
}
