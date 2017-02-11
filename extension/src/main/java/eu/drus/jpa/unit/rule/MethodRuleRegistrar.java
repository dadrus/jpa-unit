package eu.drus.jpa.unit.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.rules.MethodRule;

public final class MethodRuleRegistrar {

    private static final ServiceLoader<MethodRuleFactory> SERVICE_LOADER = ServiceLoader.load(MethodRuleFactory.class);

    private MethodRuleRegistrar() {}

    public static List<MethodRule> getRules(final ExecutionContext ctx, final ExecutionPhase phase) {

        final List<MethodRuleFactory> factories = new ArrayList<>();
        SERVICE_LOADER.forEach(mrf -> {
            if (mrf.getPhase() == phase) {
                factories.add(mrf);
            }
        });

        return factories.stream().sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1)
                .map(f -> f.createRule(ctx)).collect(Collectors.toList());
    }

    public static List<MethodRule> registerRules(final List<MethodRule> rules, final ExecutionContext ctx) {
        rules.addAll(getRules(ctx, ExecutionPhase.TEST_EXECUTION));
        rules.addAll(getRules(ctx, ExecutionPhase.DATABASE_EVALUATION));
        rules.addAll(getRules(ctx, ExecutionPhase.PERSISTENCE_PROVIDER_SETUP));
        rules.addAll(getRules(ctx, ExecutionPhase.DATABASE_INITIALIZATION));
        return rules;
    }

}
