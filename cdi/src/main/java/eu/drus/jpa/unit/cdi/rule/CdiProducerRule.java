package eu.drus.jpa.unit.cdi.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.ExecutionPhase;
import eu.drus.jpa.unit.rule.MethodRuleFactory;

public class CdiProducerRule implements MethodRule {

    public static class Factory implements MethodRuleFactory {

        @Override
        public ExecutionPhase getPhase() {
            return ExecutionPhase.TEST_EXECUTION;
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public MethodRule createRule(final ExecutionContext ctx) {
            return new CdiProducerRule(ctx);
        }
    }

    private final ExecutionContext ctx;

    public CdiProducerRule(final ExecutionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new CdiProducerStatement(ctx, base, target);
    }

}
