package eu.drus.test.persistence.rule.transaction;

import javax.persistence.EntityTransaction;

import org.junit.runners.model.Statement;

import eu.drus.test.persistence.annotation.TransactionMode.StrategyProvider;

public class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    TransactionStrategyProvider(final EntityTransaction tx) {
        this.tx = tx;
    }

    @Override
    public TransactionStrategyExecutor rollbackStrategy() {
        return (final Statement stmt) -> {
            tx.begin();
            try {
                stmt.evaluate();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor commitStrategy() {
        return (final Statement stmt) -> {
            tx.begin();
            try {
                stmt.evaluate();
            } finally {
                if (tx.isActive()) {
                    tx.commit();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor disabledStrategy() {
        return Statement::evaluate;
    }
}
