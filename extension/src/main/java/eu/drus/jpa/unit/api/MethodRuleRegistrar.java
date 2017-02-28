package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.rule.MethodRuleFactory.createRule;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.rules.MethodRule;

import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.GlobalTestFixture;
import eu.drus.jpa.unit.fixture.spi.TestFixture;

final class MethodRuleRegistrar {

    private static final ServiceLoader<TestFixture> TEST_FIXTURES = ServiceLoader.load(TestFixture.class);
    private static final ServiceLoader<GlobalTestFixture> GLOBAL_FIXTURES = ServiceLoader.load(GlobalTestFixture.class);

    private MethodRuleRegistrar() {}

    private static List<MethodRule> getGlobalTestFixtureRules(final ExecutionContext ctx) {
        final List<GlobalTestFixture> fixtures = new ArrayList<>();
        GLOBAL_FIXTURES.iterator().forEachRemaining(fixtures::add);

        return fixtures.stream().sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1)
                .map(gf -> createRule(ctx, gf)).collect(Collectors.toList());
    }

    private static List<MethodRule> getTestFixtureRules(final ExecutionContext ctx) {
        final List<TestFixture> fixtures = new ArrayList<>();
        TEST_FIXTURES.iterator().forEachRemaining(fixtures::add);

        return fixtures.stream().sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1)
                .map(f -> createRule(ctx, f)).collect(Collectors.toList());
    }

    public static List<MethodRule> registerRules(final List<MethodRule> rules, final ExecutionContext ctx) {
        rules.addAll(getTestFixtureRules(ctx));
        rules.addAll(getGlobalTestFixtureRules(ctx));
        return rules;
    }

}
