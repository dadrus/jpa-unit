package eu.drus.jpa.unit.rule;

import org.junit.rules.MethodRule;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.GlobalTestFixture;
import eu.drus.jpa.unit.fixture.spi.TestFixture;

public final class MethodRuleFactory {

    private MethodRuleFactory() {}

    public static MethodRule createRule(final ExecutionContext ctx, final GlobalTestFixture fixture) {
        return (base, method, target) -> new TestClassStatement(ctx, fixture, base, target);

    }

    public static MethodRule createRule(final ExecutionContext ctx, final TestFixture fixture) {
        return (base, method, target) -> new TestMethodStatement(ctx, fixture, base, method, target);
    }
}
