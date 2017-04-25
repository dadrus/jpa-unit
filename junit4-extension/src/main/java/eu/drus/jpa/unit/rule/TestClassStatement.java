package eu.drus.jpa.unit.rule;

import java.util.List;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class TestClassStatement extends Statement {

    private final ExecutionContext ctx;
    private final Statement base;
    private final Object target;
    private final TestClassDecorator decorator;

    private final String beforeAllKey;
    private final String counterKey;

    public TestClassStatement(final ExecutionContext ctx, final TestClassDecorator decorator, final Statement base, final Object target) {
        this.ctx = ctx;
        this.decorator = decorator;
        this.base = base;
        this.target = target;

        beforeAllKey = target.getClass().getName() + "[" + decorator.hashCode() + "].BeforeAllRun";
        counterKey = target.getClass().getName() + "[" + decorator.hashCode() + "].Counter";
    }

    @Override
    public void evaluate() throws Throwable {
        synchronized (ctx) {
            beforeAll();
        }

        try {
            base.evaluate();
        } finally {
            synchronized (ctx) {
                afterAll();
            }
        }
    }

    private void beforeAll() throws Exception {
        final Boolean isBeforeAllRun = (Boolean) ctx.getData(beforeAllKey);
        if (isBeforeAllRun == null || !isBeforeAllRun) {
            decorator.beforeAll(ctx, target.getClass());
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
            decorator.afterAll(ctx, target.getClass());
        }
    }
}
