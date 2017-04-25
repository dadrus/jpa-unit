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
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class JpaUnit implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

    private static final Comparator<TestDecorator> BEFORE_COMPARATOR = (a, b) -> a.getPriority() - b.getPriority();
    private static final Comparator<TestDecorator> AFTER_COMPARATOR = (a, b) -> b.getPriority() - a.getPriority();

    @Override
    public void beforeAll(final ContainerExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = getClassDecorators().stream().sorted(BEFORE_COMPARATOR).iterator();
        while (it.hasNext()) {
            it.next().beforeAll(ctx, testClass);
        }
    }

    @Override
    public void afterAll(final ContainerExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = getClassDecorators().stream().sorted(AFTER_COMPARATOR).iterator();
        while (it.hasNext()) {
            it.next().afterAll(ctx, testClass);
        }
    }

    @Override
    public void beforeEach(final TestExtensionContext context) throws Exception {
        final TestMethodInvocation invocation = createTestMethodInvocation(context, true);

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream().sorted(BEFORE_COMPARATOR).iterator();
        while (it.hasNext()) {
            it.next().beforeTest(invocation);
        }
    }

    @Override
    public void afterEach(final TestExtensionContext context) throws Exception {
        final TestMethodInvocation invocation = createTestMethodInvocation(context, true);

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream().sorted(AFTER_COMPARATOR).iterator();
        while (it.hasNext()) {
            it.next().afterTest(invocation);
        }
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final TestMethodInvocation invocation = createTestMethodInvocation(context, false);

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream().sorted(BEFORE_COMPARATOR).iterator();
        while (it.hasNext()) {
            it.next().processInstance(testInstance, invocation);
        }
    }

    private TestMethodInvocation createTestMethodInvocation(final ExtensionContext context, final boolean considerExceptions) {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        return new TestMethodInvocation() {

            @Override
            public Method getMethod() {
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
                return considerExceptions ? ((TestExtensionContext) context).getTestException().isPresent() : false;
            }
        };
    }
}
