package eu.drus.jpa.unit.rule.context;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.ExecutionPhase;
import eu.drus.jpa.unit.rule.MethodRuleFactory;

public class PersistenceContextRule implements MethodRule {

    public static class Factory implements MethodRuleFactory {

        @Override
        public ExecutionPhase getPhase() {
            return ExecutionPhase.PERSISTENCE_PROVIDER_SETUP;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public MethodRule createRule(final ExecutionContext ctx) {
            return new PersistenceContextRule(ctx);
        }
    }

    private final ExecutionContext ctx;

    public PersistenceContextRule(final ExecutionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new PersistenceContextStatement(ctx, base, target);
    }

}
