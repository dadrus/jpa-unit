package eu.drus.test.persistence.transaction;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.junit.runners.model.Statement;

import eu.drus.test.persistence.annotation.TransactionMode.StrategyProvider;

public class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    public TransactionStrategyProvider(final EntityTransaction tx) {
        this.tx = tx;
    }

    @Override
    public TransactionStrategyExecutor rollbackStrategy() {
        return new RollbackTransactionStrategyExecutor();
    }

    @Override
    public TransactionStrategyExecutor commitStrategy() {
        return new CommitTransactionStrategyExecutor();
    }

    @Override
    public TransactionStrategyExecutor disabledStrategy() {
        return new NoTransactionStrategyExecutor();
    }

    private void beginTransaction() {
        tx.begin();
    }

    private void commitTransaction() {
        try {
            if (tx.isActive()) {
                tx.commit();
            }
        } catch (final PersistenceException e) {
            // TODO: log
        }
    }

    private void rollbackTransaction() {
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } catch (final PersistenceException e) {
            // TODO: log
        }
    }

    private class RollbackTransactionStrategyExecutor implements TransactionStrategyExecutor {

        @Override
        public void execute(final Statement statement) throws Throwable {
            beginTransaction();
            try {
                statement.evaluate();
            } finally {
                commitTransaction();
            }
        }
    }

    private class CommitTransactionStrategyExecutor implements TransactionStrategyExecutor {

        @Override
        public void execute(final Statement statement) throws Throwable {
            beginTransaction();
            try {
                statement.evaluate();
            } finally {
                rollbackTransaction();
            }
        }
    }

    private static class NoTransactionStrategyExecutor implements TransactionStrategyExecutor {

        @Override
        public void execute(final Statement statement) throws Throwable {
            statement.evaluate();
        }
    }

}
