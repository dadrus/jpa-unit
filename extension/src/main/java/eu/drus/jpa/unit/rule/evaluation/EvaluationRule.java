package eu.drus.jpa.unit.rule.evaluation;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.dbunit.DatabaseConnectionFactory;
import eu.drus.jpa.unit.core.dbunit.DbFeatureFactory;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.ExecutionPhase;
import eu.drus.jpa.unit.rule.MethodRuleFactory;

public class EvaluationRule implements MethodRule {

    public static class Factory implements MethodRuleFactory {

        @Override
        public ExecutionPhase getPhase() {
            return ExecutionPhase.DATABASE_EVALUATION;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public MethodRule createRule(final ExecutionContext ctx) {
            return new EvaluationRule(ctx);
        }
    }

    private final ExecutionContext ctx;
    private final DatabaseConnectionFactory connectionFactory;

    public EvaluationRule(final ExecutionContext ctx) {
        this.ctx = ctx;
        connectionFactory = new DatabaseConnectionFactory(ctx.getDataBaseConnectionProperties());
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver featureResolver = ctx.createFeatureResolver(method.getMethod(), target.getClass());
        return new EvaluationStatement(connectionFactory, new DbFeatureFactory(featureResolver), base);
    }
}
