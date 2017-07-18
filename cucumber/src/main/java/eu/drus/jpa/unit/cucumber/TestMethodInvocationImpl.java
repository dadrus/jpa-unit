package eu.drus.jpa.unit.cucumber;

import java.lang.reflect.Method;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

class TestMethodInvocationImpl implements TestMethodInvocation {

    private final Class<?> clazz;
    private final Method method;
    private final JpaUnitContext ctx;
    private Exception e;

    TestMethodInvocationImpl(final Class<?> clazz, final Method method) {
        this.clazz = clazz;
        this.method = method;
        ctx = JpaUnitContext.getInstance(clazz);
    }

    @Override
    public Class<?> getTestClass() {
        return clazz;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public ExecutionContext getContext() {
        return ctx;
    }

    @Override
    public boolean hasErrors() {
        return e != null;
    }

    public void setTestException(final Exception e) {
        this.e = e;
    }
}