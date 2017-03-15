package eu.drus.jpa.unit.decorator.jpa;

interface TransactionStrategyExecutor {

    void begin();

    void commit();
}
