package eu.drus.jpa.unit.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.TestFixture;

public class FixtureRule implements MethodRule {

    private final ExecutionContext ctx;
    private final TestFixture fixture;

    public FixtureRule(final ExecutionContext ctx, final TestFixture fixture) {
        this.ctx = ctx;
        this.fixture = fixture;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new FixtureStatement(ctx, fixture, base, method, target);
    }

}
