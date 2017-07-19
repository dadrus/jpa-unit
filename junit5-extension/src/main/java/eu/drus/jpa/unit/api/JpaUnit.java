package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.core.DecoratorRegistrar.getClassDecorators;
import static eu.drus.jpa.unit.core.DecoratorRegistrar.getMethodDecorators;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class JpaUnit implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Comparator<TestDecorator> BEFORE_COMPARATOR = (a, b) -> a.getPriority() - b.getPriority();
    private static final Comparator<TestDecorator> AFTER_COMPARATOR = (a, b) -> b.getPriority() - a.getPriority();

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = classDecoratorIterator(ctx, BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeAll(ctx, testClass);
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = classDecoratorIterator(ctx, AFTER_COMPARATOR);
        while (it.hasNext()) {
            it.next().afterAll(ctx, testClass);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final TestMethodInvocation invocation = createTestMethodInvocation(context, true);

        final Iterator<TestMethodDecorator> it = methodDecoratorIterator(invocation.getContext(), BEFORE_COMPARATOR);
        while (it.hasNext()) {
            it.next().beforeTest(invocation);
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final TestMethodInvocation invocation = createTestMethodInvocation(context, true);

        final Iterator<TestMethodDecorator> it = methodDecoratorIterator(invocation.getContext(), AFTER_COMPARATOR);
        while (it.hasNext()) {
            it.next().afterTest(invocation);
        }
    }

    private Iterator<TestMethodDecorator> methodDecoratorIterator(final ExecutionContext ctx, final Comparator<TestDecorator> comparator) {
        return getMethodDecorators().stream().filter(d -> d.isConfigurationSupported(ctx)).sorted(comparator).iterator();
    }

    private Iterator<TestClassDecorator> classDecoratorIterator(final ExecutionContext ctx, final Comparator<TestDecorator> comparator) {
        return getClassDecorators().stream().filter(d -> d.isConfigurationSupported(ctx)).sorted(comparator).iterator();
    }

    private TestMethodInvocation createTestMethodInvocation(final ExtensionContext context, final boolean considerExceptions) {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        return new TestMethodInvocation() {

            @Override
            public Method getTestMethod() {
                return context.getTestMethod().get();
            }

            @Override
            public ExecutionContext getContext() {
                return ctx;
            }

            @Override
            public Class<?> getTestClass() {
                return context.getTestClass().get();
            }

            @Override
            public boolean hasErrors() {
                return considerExceptions ? context.getExecutionException().isPresent() : false;
            }

            @Override
            public FeatureResolver getFeatureResolver() {
                return FeatureResolver.newFeatureResolver(getTestMethod(), getTestClass()).build();
            }

            @Override
            public Object getTestInstance() {
                return context.getTestInstance().get();
            }
        };
    }
}
