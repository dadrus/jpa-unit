package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.TestFixture;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

public class TestMethodStatement extends Statement implements TestInvocation {

    private final ExecutionContext ctx;
    private final TestFixture fixture;
    private final Statement base;
    private final FrameworkMethod method;
    private final Object target;

    public TestMethodStatement(final ExecutionContext ctx, final TestFixture fixture, final Statement base, final FrameworkMethod method,
            final Object target) {
        this.ctx = ctx;
        this.fixture = fixture;
        this.base = base;
        this.method = method;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        fixture.apply(this);
    }

    @Override
    public void proceed() throws Throwable {
        base.evaluate();
    }

    @Override
    public Method getMethod() {
        return method.getMethod();
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public ExecutionContext getContext() {
        return ctx;
    }

}
