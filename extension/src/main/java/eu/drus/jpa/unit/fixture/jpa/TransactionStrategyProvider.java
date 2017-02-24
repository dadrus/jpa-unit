package eu.drus.jpa.unit.fixture.jpa;

import javax.persistence.EntityTransaction;

import eu.drus.jpa.unit.api.TransactionMode.StrategyProvider;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    TransactionStrategyProvider(final EntityTransaction tx) {
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
