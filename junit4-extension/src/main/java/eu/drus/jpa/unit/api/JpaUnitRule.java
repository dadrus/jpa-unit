package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.api.MethodRuleRegistrar.registerRules;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class JpaUnitRule implements MethodRule {

    private final JpaUnitContext ctx;

    public JpaUnitRule(final Class<?> clazz) {
        ctx = JpaUnitContext.getInstance(new TestClass(clazz));
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
        return registerRules(new ArrayList<>(), ctx);
    }
}
