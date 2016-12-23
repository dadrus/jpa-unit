package eu.drus.jpa.unit.rule.evaluation;

import java.util.Map;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.dbunit.DatabaseConnectionFactory;
import eu.drus.jpa.unit.core.dbunit.DbFeatureFactory;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;

public class EvaluationRule implements MethodRule {

    private final DatabaseConnectionFactory connectionFactory;
    private final FeatureResolverFactory featureResolverFactory;

    public EvaluationRule(final FeatureResolverFactory featureResolverFactory, final Map<String, Object> properties) {
        this.featureResolverFactory = featureResolverFactory;
        connectionFactory = new DatabaseConnectionFactory(properties);
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver featureResolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        return new EvaluationStatement(connectionFactory, new DbFeatureFactory(featureResolver), base);
    }
}
