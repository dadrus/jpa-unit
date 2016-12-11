package eu.drus.test.persistence.rule.evaluation;

import java.io.IOException;
import java.util.Map;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.JpaUnitException;
import eu.drus.test.persistence.core.dbunit.DatabaseConnectionFactory;
import eu.drus.test.persistence.core.dbunit.DbFeatureFactory;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

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
        try {
            return new EvaluationStatement(connectionFactory, new DbFeatureFactory(featureResolver), base);
        } catch (final IOException e) {
            throw new JpaUnitException("Failed to create statement", e);
        }
    }
}
