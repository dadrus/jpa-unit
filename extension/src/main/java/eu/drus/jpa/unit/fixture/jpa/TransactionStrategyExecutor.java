package eu.drus.jpa.unit.fixture.jpa;

import eu.drus.jpa.unit.fixture.spi.TestInvocation;

@FunctionalInterface
interface TransactionStrategyExecutor {
    void execute(final TestInvocation invocation) throws Throwable;
}
