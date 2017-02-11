package eu.drus.jpa.unit.rule.transaction;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.ExecutionPhase;
import eu.drus.jpa.unit.rule.MethodRuleFactory;

public class TransactionalRule implements MethodRule {

    public static class Factory implements MethodRuleFactory {

        @Override
        public ExecutionPhase getPhase() {
            return ExecutionPhase.TEST_EXECUTION;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public MethodRule createRule(final ExecutionContext ctx) {
            return new TransactionalRule(ctx);
        }
    }

    private final ExecutionContext ctx;

    public TransactionalRule(final ExecutionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new TransactionalStatement(ctx, base, method, target);
    }
}
