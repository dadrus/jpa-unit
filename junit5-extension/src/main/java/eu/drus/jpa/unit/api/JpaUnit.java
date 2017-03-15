package eu.drus.jpa.unit.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class JpaUnit implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

    private static final ServiceLoader<TestClassDecorator> CLASS_DECORATORS = ServiceLoader.load(TestClassDecorator.class);
    private static final ServiceLoader<TestMethodDecorator> METHOD_DECORATORS = ServiceLoader.load(TestMethodDecorator.class);

    private static List<TestClassDecorator> getClassDecorators(final Comparator<? super TestClassDecorator> c) {
        final List<TestClassDecorator> decorators = new ArrayList<>();
        CLASS_DECORATORS.iterator().forEachRemaining(decorators::add);
        decorators.sort(c);
        return decorators;
    }

    private static List<TestMethodDecorator> getMethodDecorators(final Comparator<? super TestMethodDecorator> c) {
        final List<TestMethodDecorator> decorators = new ArrayList<>();
        METHOD_DECORATORS.iterator().forEachRemaining(decorators::add);
        decorators.sort(c);
        return decorators;
    }

    @Override
    public void beforeAll(final ContainerExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final List<TestClassDecorator> globalFixtures = getClassDecorators(
                (a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1);

        for (final TestClassDecorator d : globalFixtures) {
            d.beforeAll(ctx, testClass);
        }
    }

    @Override
    public void afterAll(final ContainerExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().get();
        final JpaUnitContext ctx = JpaUnitContext.getInstance(testClass);

        final List<TestClassDecorator> globalFixtures = getClassDecorators(
                (a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1);

        for (final TestClassDecorator d : globalFixtures) {
            d.afterAll(ctx, testClass);
        }
    }

    @Override
    public void beforeEach(final TestExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final List<TestMethodDecorator> decorators = getMethodDecorators(
                (a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1);

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

        for (final TestMethodDecorator d : decorators) {
            d.beforeTest(invocation);
        }
    }

    @Override
    public void afterEach(final TestExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final List<TestMethodDecorator> decorators = getMethodDecorators(
                (a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1);

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

        for (final TestMethodDecorator d : decorators) {
            d.afterTest(invocation);
        }
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final List<TestMethodDecorator> decorators = getMethodDecorators(
                (a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1);

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

        for (final TestMethodDecorator d : decorators) {
            d.processInstance(testInstance, invocation);
        }
    }
}
