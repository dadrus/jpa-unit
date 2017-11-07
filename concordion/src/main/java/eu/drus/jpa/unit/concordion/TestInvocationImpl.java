package eu.drus.jpa.unit.concordion;

import java.lang.reflect.Method;
import java.util.Optional;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

class TestInvocationImpl implements TestInvocation {

    private final Class<?> clazz;
    private final JpaUnitContext ctx;
    private final FeatureResolver resolver;
    private Object instance;
    private Method method;
    private Exception e;

    TestInvocationImpl(final Class<?> clazz, final FeatureResolver resolver) {
        this.clazz = clazz;
        this.resolver = resolver;
        ctx = JpaUnitContext.getInstance(clazz);
    }

    TestInvocationImpl(final Object instance, final Method method, final FeatureResolver resolver) {
        this(instance.getClass(), resolver);
        this.instance = instance;
        this.method = method;
    }

    @Override
    public Class<?> getTestClass() {
        return clazz;
    }

    @Override
    public Optional<Method> getTestMethod() {
        return Optional.ofNullable(method);
    }

    @Override
    public ExecutionContext getContext() {
        return ctx;
    }

    @Override
    public Optional<Throwable> getException() {
        return Optional.ofNullable(e);
    }

    public void setTestException(final Exception e) {
        this.e = e;
    }

    @Override
    public FeatureResolver getFeatureResolver() {
        return resolver;
    }

    @Override
    public Optional<Object> getTestInstance() {
        return Optional.ofNullable(instance);
    }
}