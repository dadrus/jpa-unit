package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

public class TestMethodStatement extends Statement implements TestInvocation {

    private final ExecutionContext ctx;
    private final DecoratorExecutor executor;
    private final Statement base;
    private final FrameworkMethod method;
    private final Object target;
    private Throwable thrownException;

    public TestMethodStatement(final ExecutionContext ctx, final DecoratorExecutor executor, final Statement base,
            final FrameworkMethod method, final Object target) {
        this.ctx = ctx;
        this.executor = executor;
        this.base = base;
        this.method = method;
        this.target = target;
        thrownException = null;
    }

    @Override
    public void evaluate() throws Throwable {
        executor.processBefore(this);
        try {
            base.evaluate();
        } catch (final Throwable t) {
            thrownException = t;
            throw t;
        } finally {
            executor.processAfter(this);
        }
    }

    @Override
    public Optional<Method> getTestMethod() {
        return Optional.of(method.getMethod());
    }

    @Override
    public ExecutionContext getContext() {
        return ctx;
    }

    @Override
    public Class<?> getTestClass() {
        return target.getClass();
    }

    @Override
    public Optional<Throwable> getException() {
        return Optional.ofNullable(thrownException);
    }

    @Override
    public FeatureResolver getFeatureResolver() {
        return FeatureResolver.newFeatureResolver(target.getClass()).withTestMethod(method.getMethod()).build();
    }

    @Override
    public Optional<Object> getTestInstance() {
        return Optional.of(target);
    }

}
