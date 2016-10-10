package eu.drus.test.persistence.rule.transaction;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionStrategyProviderTest {

    @Mock
    private EntityTransaction tx;

    @Mock
    private Statement statement;

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
        executor.execute(statement);

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
        verify(tx).isActive();
        verify(tx).rollback();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionRollbackStrategyExecutionForActiveTransactionForStatementThrowingException() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final RuntimeException error = new RuntimeException("Error while executing statement");
        doThrow(error).when(statement).evaluate();
        final TransactionStrategyExecutor executor = provider.rollbackStrategy();

        // WHEN
        try {
            executor.execute(statement);
            fail("RuntimeException expected");
        } catch (final RuntimeException e) {
            assertThat(e, equalTo(error));
        }

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
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
        executor.execute(statement);

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
        verify(tx).isActive();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionForActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        executor.execute(statement);

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
        verify(tx).isActive();
        verify(tx).commit();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionForActiveTransactionForStatementThrowingException() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final RuntimeException error = new RuntimeException("Error while executing statement");
        doThrow(error).when(statement).evaluate();
        when(tx.isActive()).thenReturn(Boolean.TRUE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        try {
            executor.execute(statement);
            fail("RuntimeException expected");
        } catch (final RuntimeException e) {
            assertThat(e, equalTo(error));
        }

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
        verify(tx).isActive();
        verify(tx).commit();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testTransactionCommitStrategyExecutionWithoutActiveTransaction() throws Throwable {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.FALSE);
        final TransactionStrategyExecutor executor = provider.commitStrategy();

        // WHEN
        executor.execute(statement);

        // THEN
        verify(tx).begin();
        verify(statement).evaluate();
        verify(tx).isActive();
        verify(tx, times(0)).commit();
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testNoTransactionStrategyExecution() throws Throwable {
        // GIVEN
        final TransactionStrategyExecutor executor = provider.disabledStrategy();

        // WHEN
        executor.execute(statement);

        // THEN
        verify(statement).evaluate();
        verifyZeroInteractions(tx);
    }
}
