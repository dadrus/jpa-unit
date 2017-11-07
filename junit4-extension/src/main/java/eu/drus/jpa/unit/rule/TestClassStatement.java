package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

public class TestClassStatement extends Statement implements TestInvocation {

    private final ExecutionContext ctx;
    private final Statement base;
    private final Object target;
    private final DecoratorExecutor executor;
    private final FeatureResolver resolver;

    private final String beforeAllKey;
    private final String counterKey;
    private Throwable thrownException;

    public TestClassStatement(final ExecutionContext ctx, final DecoratorExecutor executor, final Statement base, final Object target) {
        this.ctx = ctx;
        this.executor = executor;
        this.base = base;
        this.target = target;
        resolver = FeatureResolver.newFeatureResolver(target.getClass()).build();

        beforeAllKey = target.getClass().getName() + ".BeforeAllRun";
        counterKey = target.getClass().getName() + ".Counter";
    }

    @Override
    public void evaluate() throws Throwable {
        synchronized (ctx) {
            beforeAll();
        }

        try {
            base.evaluate();
        } catch (final Throwable t) {
            thrownException = t;
            throw t;
        } finally {
            synchronized (ctx) {
                afterAll();
            }
        }
    }

    private void beforeAll() throws Exception {
        final Boolean isBeforeAllRun = (Boolean) ctx.getData(beforeAllKey);
        if (isBeforeAllRun == null) {
            executor.processBeforeAll(this);
        }
        ctx.storeData(beforeAllKey, Boolean.TRUE);
    }

    private void afterAll() throws Exception {
        Integer counter = (Integer) ctx.getData(counterKey);
        if (counter == null) {
            counter = 0;
        }
        ctx.storeData(counterKey, ++counter);
        final List<FrameworkMethod> testMethods = new TestClass(target.getClass()).getAnnotatedMethods(Test.class);
        if (counter >= testMethods.size()) {
            executor.processAfterAll(this);
        }
    }

    @Override
    public Class<?> getTestClass() {
        return target.getClass();
    }

    @Override
    public ExecutionContext getContext() {
        return ctx;
    }

    @Override
    public Optional<Method> getTestMethod() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getTestInstance() {
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> getException() {
        return Optional.ofNullable(thrownException);
    }

    @Override
    public FeatureResolver getFeatureResolver() {
        return resolver;
    }
}
