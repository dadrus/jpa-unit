package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.rule.MethodRuleRegistrar.registerRules;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;

public class JpaUnitRule implements MethodRule {

    private final JpaUnitContext ctx;
    private final DecoratorExecutor executor;

    public JpaUnitRule(final Class<?> clazz) {
        ctx = JpaUnitContext.getInstance(clazz);
        executor = new DecoratorExecutor();
    }

    @Override
    public Statement apply(final Statement result, final FrameworkMethod method, final Object target) {
        Statement lastResult = result;

        for (final MethodRule rule : getRules()) {
            lastResult = rule.apply(lastResult, method, target);
        }

        return lastResult;
    }

    private List<MethodRule> getRules() {
        return registerRules(new ArrayList<>(), executor, ctx);
    }
}
