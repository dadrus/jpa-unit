package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityTransaction;

import eu.drus.jpa.unit.api.TransactionMode.StrategyProvider;

class TransactionStrategyProvider implements StrategyProvider<TransactionStrategyExecutor> {

    private final EntityTransaction tx;

    TransactionStrategyProvider(final EntityTransaction tx) {
        this.tx = tx;
    }

    @Override
    public TransactionStrategyExecutor rollbackStrategy() {
        return new TransactionStrategyExecutor() {

            @Override
            public void begin() {
                tx.begin();
            }

            @Override
            public void commit() {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor commitStrategy() {
        return new TransactionStrategyExecutor() {

            @Override
            public void begin() {
                tx.begin();
            }

            @Override
            public void commit() {
                if (tx.isActive() && !tx.getRollbackOnly()) {
                    tx.commit();
                }
            }
        };
    }

    @Override
    public TransactionStrategyExecutor disabledStrategy() {
        return new TransactionStrategyExecutor() {

            @Override
            public void commit() {
                // nothing to do
            }

            @Override
            public void begin() {
                // nothing to do
            }
        };
    }
}
