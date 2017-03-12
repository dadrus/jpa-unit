package eu.drus.jpa.unit.decorator.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.drus.jpa.unit.decorator.dbunit.DataSeedStrategyProvider;

public class DataSeedStrategyProviderTest {

    private static final DataSeedStrategyProvider STRATEGY_PROVIDER = new DataSeedStrategyProvider();

    @Test
    public void testCleanInsertStrategy() {
        final DatabaseOperation operation = STRATEGY_PROVIDER.cleanInsertStrategy();

        assertThat(operation, equalTo(DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    public void testInsertStrategy() {
        final DatabaseOperation operation = STRATEGY_PROVIDER.insertStrategy();

        assertThat(operation, equalTo(DatabaseOperation.INSERT));
    }

    @Test
    public void testRefreshStrategy() {
        final DatabaseOperation operation = STRATEGY_PROVIDER.refreshStrategy();

        assertThat(operation, equalTo(DatabaseOperation.REFRESH));
    }

    @Test
    public void testUpdateStrategy() {
        final DatabaseOperation operation = STRATEGY_PROVIDER.updateStrategy();

        assertThat(operation, equalTo(DatabaseOperation.UPDATE));
    }
}
