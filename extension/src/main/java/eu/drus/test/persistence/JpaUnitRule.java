package eu.drus.test.persistence;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;
import eu.drus.test.persistence.rule.cache.SecondLevelCacheRule;
import eu.drus.test.persistence.rule.context.PersistenceContextRule;
import eu.drus.test.persistence.rule.evaluation.EvaluationRule;
import eu.drus.test.persistence.rule.transaction.TransactionalRule;

public class JpaUnitRule implements MethodRule {

    private final JpaUnitContext ctx;

    public JpaUnitRule(final Class<?> clazz) {
        ctx = JpaUnitContext.getInstance(new TestClass(clazz));
    }

    @Override
    public Statement apply(final Statement result, final FrameworkMethod method, final Object target) {
        final FeatureResolverFactory featureResolverFactory = new FeatureResolverFactory();

        final List<MethodRule> rules = new ArrayList<>();
        rules.add(new TransactionalRule(featureResolverFactory, ctx.getPersistenceField()));
        rules.add(new EvaluationRule(featureResolverFactory, ctx.getProperties()));
        rules.add(new SecondLevelCacheRule(featureResolverFactory, ctx));
        rules.add(new PersistenceContextRule(ctx, ctx.getPersistenceField()));

        Statement lastResult = null;

        for (final MethodRule rule : rules) {
            lastResult = rule.apply(result, method, target);
        }

        return lastResult;
    }
}
