package eu.drus.jpa.unit.rule.transaction;

import javax.persistence.EntityTransaction;

import eu.drus.jpa.unit.annotation.TransactionMode.StrategyProvider;
import eu.drus.jpa.unit.rule.TestInvocation;

public class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    public TransactionStrategyProvider(final EntityTransaction tx) {
        this.tx = tx;
    }

    @Override
    public TransactionStrategyExecutor rollbackStrategy() {
        return (final TestInvocation invocation) -> {
            tx.begin();
            try {
                invocation.proceed();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor commitStrategy() {
        return (final TestInvocation invocation) -> {
            tx.begin();
            try {
                invocation.proceed();
            } finally {
                if (tx.isActive()) {
                    tx.commit();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor disabledStrategy() {
        return TestInvocation::proceed;
    }
}
