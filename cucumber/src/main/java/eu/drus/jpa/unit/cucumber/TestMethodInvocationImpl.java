package eu.drus.jpa.unit.cucumber;

import java.lang.reflect.Method;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

class TestMethodInvocationImpl implements TestMethodInvocation {

    private final Object instance;
    private final Class<?> clazz;
    private final Method method;
    private final JpaUnitContext ctx;
    private Exception e;
    private FeatureResolver resolver;

    TestMethodInvocationImpl(final Object instance, final Method method, final FeatureResolver resolver) {
        this.instance = instance;
        clazz = instance.getClass();
        this.method = method;
        this.resolver = resolver;
        ctx = JpaUnitContext.getInstance(clazz);
    }

    @Override
    public Class<?> getTestClass() {
        return clazz;
    }

    @Override
    public Method getTestMethod() {
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

    @Override
    public FeatureResolver getFeatureResolver() {
        return resolver;
    }

    @Override
    public Object getTestInstance() {
        return instance;
    }
}