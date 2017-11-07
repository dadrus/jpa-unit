package eu.drus.jpa.unit.api;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

public class JpaUnit implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private final DecoratorExecutor executor = new DecoratorExecutor();

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        executor.processBeforeAll(createTestMethodInvocation(context, true));
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        executor.processAfterAll(createTestMethodInvocation(context, true));
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        executor.processBefore(createTestMethodInvocation(context, true));
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        executor.processAfter(createTestMethodInvocation(context, true));
    }

    private TestInvocation createTestMethodInvocation(final ExtensionContext context, final boolean considerExceptions) {
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        return new TestInvocation() {

            @Override
            public Optional<Method> getTestMethod() {
                return context.getTestMethod();
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
            public Optional<Throwable> getException() {
                return considerExceptions ? context.getExecutionException() : Optional.empty();
            }

            @Override
            public FeatureResolver getFeatureResolver() {
                final FeatureResolver.Builder builder = FeatureResolver.newFeatureResolver(getTestClass());
                final Optional<Method> method = getTestMethod();
                if (method.isPresent()) {
                    builder.withTestMethod(method.get());
                }
                return builder.build();
            }

            @Override
            public Optional<Object> getTestInstance() {
                return context.getTestInstance();
            }
        };
    }
}
