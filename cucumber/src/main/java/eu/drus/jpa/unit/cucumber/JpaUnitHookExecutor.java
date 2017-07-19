package eu.drus.jpa.unit.cucumber;

import static eu.drus.jpa.unit.core.DecoratorRegistrar.getClassDecorators;
import static eu.drus.jpa.unit.core.DecoratorRegistrar.getMethodDecorators;

import java.util.Comparator;
import java.util.Iterator;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class JpaUnitHookExecutor {

    private static final Comparator<TestDecorator> BEFORE_COMPARATOR = (a, b) -> a.getPriority() - b.getPriority();
    private static final Comparator<TestDecorator> AFTER_COMPARATOR = (a, b) -> b.getPriority() - a.getPriority();

    public void processBeforeAll(final Class<?> testClass) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = classDecoratorIterator(ctx, BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeAll(ctx, testClass);
        }
    }

    public void processAfterAll(final Class<?> testClass) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = classDecoratorIterator(ctx, AFTER_COMPARATOR);
        while (it.hasNext()) {
            it.next().afterAll(ctx, testClass);
        }
    }

    public void processBefore(final TestMethodInvocation invocation) throws Exception {
        final Iterator<TestMethodDecorator> it = methodDecoratorIterator(invocation.getContext(), BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeTest(invocation);
        }
    }

    public void processAfter(final TestMethodInvocation invocation) throws Exception {
        final Iterator<TestMethodDecorator> it = methodDecoratorIterator(invocation.getContext(), AFTER_COMPARATOR);
        while (it.hasNext()) {
            it.next().afterTest(invocation);
        }
    }

    private Iterator<TestClassDecorator> classDecoratorIterator(final ExecutionContext ctx, final Comparator<TestDecorator> comparator) {
        return getClassDecorators().stream().filter(d -> d.isConfigurationSupported(ctx)).sorted(comparator).iterator();
    }

    private Iterator<TestMethodDecorator> methodDecoratorIterator(final ExecutionContext ctx, final Comparator<TestDecorator> comparator) {
        return getMethodDecorators().stream().filter(d -> d.isConfigurationSupported(ctx)).sorted(comparator).iterator();
    }
}
