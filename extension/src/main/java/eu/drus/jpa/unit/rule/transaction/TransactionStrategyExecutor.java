package eu.drus.jpa.unit.rule.transaction;

import eu.drus.jpa.unit.rule.TestInvocation;

@FunctionalInterface
public interface TransactionStrategyExecutor {
    void execute(final TestInvocation invocation) throws Throwable;
}
