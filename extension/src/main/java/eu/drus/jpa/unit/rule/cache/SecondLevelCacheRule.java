package eu.drus.jpa.unit.rule.cache;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.ExecutionPhase;
import eu.drus.jpa.unit.rule.MethodRuleFactory;

public class SecondLevelCacheRule implements MethodRule {

    public static class Factory implements MethodRuleFactory {

        @Override
        public ExecutionPhase getPhase() {
            return ExecutionPhase.PERSISTENCE_PROVIDER_SETUP;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public MethodRule createRule(final ExecutionContext ctx) {
            return new SecondLevelCacheRule(ctx);
        }
    }

    private final ExecutionContext ctx;

    public SecondLevelCacheRule(final ExecutionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver resolver = ctx.createFeatureResolver(method.getMethod(), target.getClass());
        return new SecondLevelCacheStatement(resolver, ctx, base);
    }

}
