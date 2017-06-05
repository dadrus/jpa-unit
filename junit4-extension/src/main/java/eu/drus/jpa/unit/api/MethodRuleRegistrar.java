package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.core.DecoratorRegistrar.getClassDecorators;
import static eu.drus.jpa.unit.core.DecoratorRegistrar.getMethodDecorators;
import static eu.drus.jpa.unit.rule.MethodRuleFactory.createRule;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.rules.MethodRule;

import eu.drus.jpa.unit.spi.ExecutionContext;

final class MethodRuleRegistrar {

    private MethodRuleRegistrar() {}

    private static List<MethodRule> getGlobalTestFixtureRules(final ExecutionContext ctx) {
        return getClassDecorators().stream().filter(d -> d.isConfigurationSupported(ctx))
                .sorted((a, b) -> b.getPriority() - a.getPriority()).map(gf -> createRule(ctx, gf)).collect(Collectors.toList());
    }

    private static List<MethodRule> getTestFixtureRules(final ExecutionContext ctx) {
        return getMethodDecorators().stream().filter(d -> d.isConfigurationSupported(ctx))
                .sorted((a, b) -> b.getPriority() - a.getPriority()).map(f -> createRule(ctx, f)).collect(Collectors.toList());
    }

    public static List<MethodRule> registerRules(final List<MethodRule> rules, final ExecutionContext ctx) {
        rules.addAll(getTestFixtureRules(ctx));
        rules.addAll(getGlobalTestFixtureRules(ctx));
        return rules;
    }

}
