package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.neo4j.operation.Neo4JOperations;

public class DataSeedStrategyProviderTest {

    private static final DataSeedStrategyProvider STRATEGY_PROVIDER = new DataSeedStrategyProvider();

    @Test
    public void testCleanInsertStrategy() {
        assertThat(STRATEGY_PROVIDER.cleanInsertStrategy(), equalTo(Neo4JOperations.CLEAN_INSERT));
    }

    @Test
    public void testInsertStrategy() {
        assertThat(STRATEGY_PROVIDER.insertStrategy(), equalTo(Neo4JOperations.INSERT));
    }

    @Test
    public void testRefreshStrategy() {
        assertThat(STRATEGY_PROVIDER.refreshStrategy(), equalTo(Neo4JOperations.REFRESH));
    }

    @Test
    public void testUpdateStrategy() {
        assertThat(STRATEGY_PROVIDER.updateStrategy(), equalTo(Neo4JOperations.UPDATE));
    }
}
