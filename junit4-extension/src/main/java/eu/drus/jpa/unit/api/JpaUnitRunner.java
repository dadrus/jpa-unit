package eu.drus.jpa.unit.api;

import static eu.drus.jpa.unit.api.MethodRuleRegistrar.registerRules;

import java.util.List;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;

public class JpaUnitRunner extends BlockJUnit4ClassRunner {

    private JpaUnitContext ctx;

    public JpaUnitRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);

        ctx = JpaUnitContext.getInstance(clazz);

        final List<FrameworkField> ruleFields = getTestClass().getAnnotatedFields(Rule.class);
        if (ruleFields.stream().anyMatch(f -> f.getType().equals(JpaUnitRule.class))) {
            throw new InitializationError("JpaUnitRunner and JpaUnitRule exclude each other");
        }
    }

    @Override
    protected List<MethodRule> rules(final Object target) {
        return registerRules(super.rules(target), ctx);
    }
}
