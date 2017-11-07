package eu.drus.jpa.unit.rule;

import java.util.List;

import org.junit.rules.MethodRule;

import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.ExecutionContext;

public final class MethodRuleRegistrar {

    private MethodRuleRegistrar() {}

    public static List<MethodRule> registerRules(final List<MethodRule> rules, final DecoratorExecutor executor,
            final ExecutionContext ctx) {
        rules.add((base, method, target) -> new TestMethodStatement(ctx, executor, base, method, target));
        rules.add((base, method, target) -> new TestClassStatement(ctx, executor, base, target));
        return rules;
    }

}
