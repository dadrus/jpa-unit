package eu.drus.test.persistence.rule.transaction;

import org.junit.runners.model.Statement;

@FunctionalInterface
interface TransactionStrategyExecutor {
    void execute(final Statement statement) throws Throwable;
}
