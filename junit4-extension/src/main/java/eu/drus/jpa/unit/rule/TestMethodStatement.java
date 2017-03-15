package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class TestMethodStatement extends Statement implements TestMethodInvocation {

    private final ExecutionContext ctx;
    private final TestMethodDecorator decorator;
    private final Statement base;
    private final FrameworkMethod method;
    private final Object target;
    private boolean isExceptionThrown;

    public TestMethodStatement(final ExecutionContext ctx, final TestMethodDecorator decorator, final Statement base,
            final FrameworkMethod method, final Object target) {
        this.ctx = ctx;
        this.decorator = decorator;
        this.base = base;
        this.method = method;
        this.target = target;
        isExceptionThrown = false;
    }

    @Override
    public void evaluate() throws Throwable {
        decorator.processInstance(target, this);
        decorator.beforeTest(this);
        try {
            base.evaluate();
        } catch (final Throwable t) {
            isExceptionThrown = true;
            throw t;
        } finally {
            decorator.afterTest(this);
        }
    }

    @Override
    public Method getMethod() {
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

}
