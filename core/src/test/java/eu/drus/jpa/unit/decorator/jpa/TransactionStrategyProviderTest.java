package eu.drus.jpa.unit.decorator.jpa;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(MockitoJUnitRunner.class)
public class TransactionStrategyProviderTest {

    @Mock
    private EntityTransaction tx;

    @Mock
    private TestMethodInvocation invocation;

    private TransactionStrategyProvider provider;

    @Before
    public void createTransactionStrategyProvider() {
        provider = new TransactionStrategyProvider(tx);
    }

    @Test
    public void testTransactionRollbackStrategyExecutionForActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final TransactionStrategyExecutor executor = provider.rollbackStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verify(tx).begin();
        verify(tx).isActive();
        verify(tx).rollback();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionRollbackStrategyExecutionWithoutActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.FALSE);
        final TransactionStrategyExecutor executor = provider.rollbackStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verify(tx).begin();
        verify(tx).isActive();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionForActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verify(tx).begin();
        verify(tx).isActive();
        verify(tx).getRollbackOnly();
        verify(tx).commit();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionForRolledBackTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        when(tx.getRollbackOnly()).thenReturn(Boolean.TRUE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verify(tx).begin();
        verify(tx).isActive();
        verify(tx).getRollbackOnly();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionWithoutActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.FALSE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verify(tx).begin();
        verify(tx).isActive();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testNoTransactionStrategyExecution() throws Throwable {
        // GIVEN
        final TransactionStrategyExecutor executor = provider.disabledStrategy();

        // WHEN
        executor.begin();
        executor.commit();

        // THEN
        verifyZeroInteractions(tx);
    }
}
