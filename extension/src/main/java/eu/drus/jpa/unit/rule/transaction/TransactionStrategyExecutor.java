package eu.drus.jpa.unit.rule.transaction;

import org.junit.runners.model.Statement;

@FunctionalInterface
interface TransactionStrategyExecutor {
    void execute(final Statement statement) throws Throwable;
}
