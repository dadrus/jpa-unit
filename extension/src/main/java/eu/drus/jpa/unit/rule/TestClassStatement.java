package eu.drus.jpa.unit.rule;

import java.util.List;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.GlobalTestFixture;

public class TestClassStatement extends Statement {

    private final ExecutionContext ctx;
    private final Statement base;
    private final Object target;
    private final GlobalTestFixture fixture;

    private final String beforeAllKey;
    private final String counterKey;

    public TestClassStatement(final ExecutionContext ctx, final GlobalTestFixture fixture, final Statement base, final Object target) {
        this.ctx = ctx;
        this.fixture = fixture;
        this.base = base;
        this.target = target;

        beforeAllKey = target.getClass().getName() + "[" + fixture.hashCode() + "].BeforeAllRun";
        counterKey = target.getClass().getName() + "[" + fixture.hashCode() + "].Counter";
    }

    @Override
    public void evaluate() throws Throwable {
        beforeAll();

        try {
            base.evaluate();
        } finally {
            afterAll();
        }
    }

    private void beforeAll() throws Throwable {
        final Boolean isBeforeAllRun = (Boolean) ctx.getData(beforeAllKey);
        if (isBeforeAllRun == null || !isBeforeAllRun) {
            fixture.beforeAll(ctx, target);
        }
        ctx.storeData(beforeAllKey, Boolean.TRUE);
    }

    private void afterAll() throws Throwable {
        Integer counter = (Integer) ctx.getData(counterKey);
        if (counter == null) {
            counter = 0;
        }
        ctx.storeData(counterKey, ++counter);
        final List<FrameworkMethod> testMethods = new TestClass(target.getClass()).getAnnotatedMethods(Test.class);
        if (counter >= testMethods.size()) {
            fixture.afterAll(ctx, target);
        }
    }
}
