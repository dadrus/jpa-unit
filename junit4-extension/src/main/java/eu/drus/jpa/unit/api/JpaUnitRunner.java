package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.rule.MethodRuleRegistrar.registerRules;

import java.util.List;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;

import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;

public class JpaUnitRunner extends BlockJUnit4ClassRunner {

    private final DecoratorExecutor executor;

    public JpaUnitRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
        executor = new DecoratorExecutor();

        final List<FrameworkField> ruleFields = getTestClass().getAnnotatedFields(Rule.class);
        if (ruleFields.stream().anyMatch(f -> f.getType().equals(JpaUnitRule.class))) {
            throw new InitializationError("JpaUnitRunner and JpaUnitRule exclude each other");
        }
    }

    @Override
    protected List<MethodRule> rules(final Object target) {
        return registerRules(super.rules(target), executor, JpaUnitContext.getInstance(getTestClass().getJavaClass()));
    }
}
