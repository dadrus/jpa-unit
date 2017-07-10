package eu.drus.jpa.unit.rule;

import org.junit.rules.MethodRule;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public final class MethodRuleFactory {

    private MethodRuleFactory() {}

    public static MethodRule createRule(final ExecutionContext ctx, final TestClassDecorator decorator) {
        return (base, method, target) -> new TestClassStatement(ctx, decorator, base, target);
    }

    public static MethodRule createRule(final ExecutionContext ctx, final TestMethodDecorator decorator) {
        return (base, method, target) -> new TestMethodStatement(ctx, decorator, base, method, target);
    }
}