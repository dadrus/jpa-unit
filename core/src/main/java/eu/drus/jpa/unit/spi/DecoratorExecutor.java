package eu.drus.jpa.unit.spi;

import static eu.drus.jpa.unit.core.DecoratorRegistrar.getClassDecorators;
import static eu.drus.jpa.unit.core.DecoratorRegistrar.getMethodDecorators;

import java.util.Comparator;
import java.util.Iterator;

public class DecoratorExecutor {

    private static final Comparator<TestDecorator> BEFORE_COMPARATOR = (a, b) -> a.getPriority() - b.getPriority();
    private static final Comparator<TestDecorator> AFTER_COMPARATOR = (a, b) -> b.getPriority() - a.getPriority();

    public void processBeforeAll(final TestInvocation invocation) throws Exception {
        final Iterator<TestClassDecorator> it = classDecoratorIterator(invocation.getContext(), BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeAll(invocation);
        }
    }

    public void processAfterAll(final TestInvocation invocation) throws Exception {
        final Iterator<TestClassDecorator> it = classDecoratorIterator(invocation.getContext(), AFTER_COMPARATOR);
        while (it.hasNext()) {
            it.next().afterAll(invocation);
        }
    }

    public void processBefore(final TestInvocation invocation) throws Exception {
        final Iterator<TestMethodDecorator> it = methodDecoratorIterator(invocation.getContext(), BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeTest(invocation);
        }
    }

    public void processAfter(final TestInvocation invocation) throws Exception {
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
