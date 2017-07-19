package eu.drus.jpa.unit.api;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public final class TransactionSupport {

    private EntityManager em;
    private boolean flushOnCommit;
    private boolean clearOnCommit;

    private TransactionSupport(final EntityManager em, final boolean flushOnCommit, final boolean clearOnCommit) {
        this.em = em;
        this.flushOnCommit = flushOnCommit;
        this.clearOnCommit = clearOnCommit;
    }

    public static TransactionSupport newTransaction(final EntityManager em) {
        return new TransactionSupport(em, false, false);
    }

    public TransactionSupport flushContextOnCommit(final boolean flag) {
        return new TransactionSupport(em, flag, clearOnCommit);
    }

    public TransactionSupport clearContextOnCommit(final boolean flag) {
        return new TransactionSupport(em, flushOnCommit, flag);
    }

    private boolean beforeTransactionBegin(final EntityTransaction tx) {
        final boolean isActive = tx.isActive();
        if (isActive) {
            tx.commit();
        }
        return isActive;
    }

    private void transactionBegin(final EntityTransaction tx) {
        tx.begin();
    }

    private void transactionCommit(final EntityTransaction tx) {
        tx.commit();
    }

    private void afterTransactionCommit(final EntityTransaction tx, final boolean wasActive) {
        if (wasActive) {
            tx.begin();
        }
    }

    private <R, T> R execute(final Function<T, R> function) {
        final EntityTransaction tx = em.getTransaction();
        final boolean wasActive = beforeTransactionBegin(tx);
        try {
            transactionBegin(tx);
            final R ret = function.apply(null);
            transactionCommit(tx);
            if (flushOnCommit) {
                em.flush();
            }

            if (clearOnCommit) {
                em.clear();
            }

            return ret;
        } finally {
            afterTransactionCommit(tx, wasActive);
        }
    }

    public void execute(final Runnable function) {
        execute(v -> {
            function.run();
            return null;
        });
    }

    public <T> T execute(final Supplier<T> function) {
        return execute(t -> function.get());
    }
}
