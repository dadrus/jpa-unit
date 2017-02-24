package eu.drus.jpa.unit.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.GlobalTestFixture;

public class GlobalFixtureRule implements MethodRule {

    private final ExecutionContext ctx;
    private final GlobalTestFixture fixture;

    public GlobalFixtureRule(final ExecutionContext ctx, final GlobalTestFixture fixture) {
        this.ctx = ctx;
        this.fixture = fixture;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new GlobalFixtureStatement(ctx, fixture, base, target);
    }

}
