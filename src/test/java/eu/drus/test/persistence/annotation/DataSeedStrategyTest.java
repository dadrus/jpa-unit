package eu.drus.test.persistence.annotation;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSeedStrategyTest {

    @Mock
    private DataSeedStrategy.StrategyProvider<Object> strategyProvider;

    @Test
    public void testCleanInsertStrategySelection() {
        final DataSeedStrategy strategy = DataSeedStrategy.CLEAN_INSERT;

        strategy.provide(strategyProvider);

        verify(strategyProvider).cleanInsertStrategy();
    }

    @Test
    public void testInsertStrategySelection() {
        final DataSeedStrategy strategy = DataSeedStrategy.INSERT;

        strategy.provide(strategyProvider);

        verify(strategyProvider).insertStrategy();
    }

    @Test
    public void testRefreshStrategySelection() {
        final DataSeedStrategy strategy = DataSeedStrategy.REFRESH;

        strategy.provide(strategyProvider);

        verify(strategyProvider).refreshStrategy();
    }

    @Test
    public void testUpdateStrategySelection() {
        final DataSeedStrategy strategy = DataSeedStrategy.UPDATE;

        strategy.provide(strategyProvider);

        verify(strategyProvider).updateStrategy();
    }
}
