package eu.drus.test.persistence.annotation;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionModeTest {

    @Mock
    private TransactionMode.StrategyProvider<Object> strategyProvider;

    @Test
    public void testCommitStrategySelection() {
        final TransactionMode mode = TransactionMode.COMMIT;

        mode.provide(strategyProvider);

        verify(strategyProvider).commitStrategy();
    }

    @Test
    public void testDisabledStrategySelection() {
        final TransactionMode mode = TransactionMode.DISABLED;

        mode.provide(strategyProvider);

        verify(strategyProvider).disabledStrategy();
    }

    @Test
    public void testRollbackStrategySelection() {
        final TransactionMode mode = TransactionMode.ROLLBACK;

        mode.provide(strategyProvider);

        verify(strategyProvider).rollbackStrategy();
    }
}
