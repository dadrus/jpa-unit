package eu.drus.test.persistence.annotation;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CleanupStrategyTest {

    @Mock
    private CleanupStrategy.StrategyProvider<Object> strategyProvider;

    @Test
    public void testStrictStrategySelection() {
        final CleanupStrategy strategy = CleanupStrategy.STRICT;

        strategy.provide(strategyProvider);

        verify(strategyProvider).strictStrategy();
    }

    @Test
    public void testUsedRowsOnlyStrategySelection() {
        final CleanupStrategy strategy = CleanupStrategy.USED_ROWS_ONLY;

        strategy.provide(strategyProvider);

        verify(strategyProvider).usedRowsOnlyStrategy();
    }

    @Test
    public void testUsedTablesOnlyStrategySelection() {
        final CleanupStrategy strategy = CleanupStrategy.USED_TABLES_ONLY;

        strategy.provide(strategyProvider);

        verify(strategyProvider).usedTablesOnlyStrategy();
    }
}
