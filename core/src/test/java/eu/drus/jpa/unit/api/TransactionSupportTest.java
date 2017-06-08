package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.api.TransactionSupport.newTransaction;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionSupportTest {

    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction tx;

    private int value = 0;

    @Before
    public void prepareMocks() {
        when(em.getTransaction()).thenReturn(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWithReturnedValue() {
        // GIVEN

        when(tx.isActive()).thenReturn(Boolean.FALSE);

        // WHEN
        final int result = newTransaction(em).execute(() -> {
            value = 1;
            return value;
        });

        // THEN
        assertThat(value, equalTo(1));
        assertThat(result, equalTo(1));
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx).begin();
        verify(tx).commit();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWithoutHavingAnActiveTransaction() {
        // GIVEN

        when(tx.isActive()).thenReturn(Boolean.FALSE);

        // WHEN
        newTransaction(em).execute(() -> {
            value = 1;
        });

        // THEN
        assertThat(value, equalTo(1));
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx).begin();
        verify(tx).commit();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWithoutHavingAnActiveTransactionAndFlushPersistenceContext() {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.FALSE);

        // WHEN
        newTransaction(em).flushContextOnCommit(true).execute(() -> {
            value = 1;
        });

        // THEN
        assertThat(value, equalTo(1));
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx).begin();
        verify(em).flush();
        verify(tx).commit();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWithoutHavingAnActiveTransactionAndClearPersistenceContext() {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.FALSE);

        // WHEN
        newTransaction(em).clearContextOnCommit(true).execute(() -> {});

        // THEN
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx).begin();
        verify(tx).commit();
        verify(em).clear();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWhileHavingAnActiveTransaction() {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);

        // WHEN
        newTransaction(em).execute(() -> {});

        // THEN
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx, times(2)).begin();
        verify(tx, times(2)).commit();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWhileHavingAnActiveTransactionAndFlushPersistenceContext() {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);

        // WHEN
        newTransaction(em).flushContextOnCommit(true).execute(() -> {});

        // THEN
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx, times(2)).begin();
        verify(tx, times(2)).commit();
        verify(em).flush();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }

    @Test
    public void testExecutionOfNewTransactionWhileHavingAnActiveTransactionAndClearPersistenceContext() {
        // GIVEN
        when(tx.isActive()).thenReturn(Boolean.TRUE);

        // WHEN
        newTransaction(em).clearContextOnCommit(true).execute(() -> {});

        // THEN
        verify(em).getTransaction();
        verify(tx).isActive();
        verify(tx, times(2)).begin();
        verify(tx, times(2)).commit();
        verify(em).clear();
        verifyNoMoreInteractions(em);
        verifyNoMoreInteractions(tx);
    }
}
