package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class TestMethodStatement extends Statement implements TestMethodInvocation {

    private final ExecutionContext ctx;
    private final DecoratorExecutor executor;
    private final Statement base;
    private final FrameworkMethod method;
    private final Object target;
    private boolean isExceptionThrown;

    public TestMethodStatement(final ExecutionContext ctx, final DecoratorExecutor executor, final Statement base, final FrameworkMethod method,
            final Object target) {
        this.ctx = ctx;
        this.executor = executor;
        this.base = base;
        this.method = method;
        this.target = target;
        isExceptionThrown = false;
    }

    @Override
    public void evaluate() throws Throwable {
        executor.processBefore(this);
        try {
            base.evaluate();
        } catch (final Throwable t) {
            isExceptionThrown = true;
            throw t;
        } finally {
            executor.processAfter(this);
        }
    }

    @Override
    public Method getTestMethod() {
        return method.getMethod();
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
    public boolean hasErrors() {
        return isExceptionThrown;
    }

    @Override
    public FeatureResolver getFeatureResolver() {
        return FeatureResolver.newFeatureResolver(method.getMethod(), target.getClass()).build();
    }

    @Override
    public Object getTestInstance() {
        return target;
    }

}
