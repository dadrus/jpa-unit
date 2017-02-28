package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityTransaction;

import eu.drus.jpa.unit.api.TransactionMode.StrategyProvider;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    TransactionStrategyProvider(final EntityTransaction tx) {
        this.tx = tx;
    }

    @Override
    public TransactionStrategyExecutor rollbackStrategy() {
        return (final TestMethodInvocation invocation) -> {
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
        return (final TestMethodInvocation invocation) -> {
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
        return TestMethodInvocation::proceed;
    }
}
