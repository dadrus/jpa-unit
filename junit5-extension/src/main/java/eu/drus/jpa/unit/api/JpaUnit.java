package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.core.DecoratorRegistrar.getClassDecorators;
import static eu.drus.jpa.unit.core.DecoratorRegistrar.getMethodDecorators;

import java.lang.reflect.Method;
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
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class JpaUnit implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

    @Override
    public void beforeAll(final ContainerExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = getClassDecorators().stream()
                .sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1).iterator();

        while (it.hasNext()) {
            it.next().beforeAll(ctx, testClass);
        }
    }

    @Override
    public void afterAll(final ContainerExtensionContext context) throws Exception {
        final Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final Iterator<TestClassDecorator> it = getClassDecorators().stream()
                .sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1).iterator();

        while (it.hasNext()) {
            it.next().afterAll(ctx, testClass);
        }
    }

    @Override
    public void beforeEach(final TestExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream()
                .sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1).iterator();

        final TestMethodInvocation invocation = new TestMethodInvocation() {

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
                return context.getTestException().isPresent();
            }
        };

        while (it.hasNext()) {
            it.next().beforeTest(invocation);
        }
    }

    @Override
    public void afterEach(final TestExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream()
                .sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1).iterator();

        final TestMethodInvocation invocation = new TestMethodInvocation() {
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
                return context.getTestException().isPresent();
            }
        };

        while (it.hasNext()) {
            it.next().afterTest(invocation);
        }
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final Iterator<TestMethodDecorator> it = getMethodDecorators().stream()
                .sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1).iterator();

        final TestMethodInvocation invocation = new TestMethodInvocation() {

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
                return false;
            }
        };

        while (it.hasNext()) {
            it.next().processInstance(testInstance, invocation);
        }
    }
}
