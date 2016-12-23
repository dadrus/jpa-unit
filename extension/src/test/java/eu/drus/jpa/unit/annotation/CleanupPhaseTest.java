package eu.drus.jpa.unit.annotation;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.annotation.CleanupPhase;

@RunWith(MockitoJUnitRunner.class)
public class CleanupPhaseTest {

    @Mock
    private CleanupPhase.StrategyProvider<Object> strategyProvider;

    @Test
    public void testAfterStrategySelection() {
        final CleanupPhase phase = CleanupPhase.AFTER;

        phase.provide(strategyProvider);

        verify(strategyProvider).afterStrategy();
    }

    @Test
    public void testBeforeStrategySelection() {
        final CleanupPhase phase = CleanupPhase.BEFORE;

        phase.provide(strategyProvider);

        verify(strategyProvider).beforeStrategy();
    }

    @Test
    public void testNoneStrategySelection() {
        final CleanupPhase phase = CleanupPhase.NONE;

        phase.provide(strategyProvider);

        verify(strategyProvider).noneStrategy();
    }
}
