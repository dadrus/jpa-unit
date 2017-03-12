package eu.drus.jpa.unit.decorator.jpa;

import eu.drus.jpa.unit.spi.TestMethodInvocation;

@FunctionalInterface
interface TransactionStrategyExecutor {
    void execute(final TestMethodInvocation invocation) throws Exception;
}
