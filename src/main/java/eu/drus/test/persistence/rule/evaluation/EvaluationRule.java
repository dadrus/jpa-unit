package eu.drus.test.persistence.rule.evaluation;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.JpaTestException;
import eu.drus.test.persistence.core.dbunit.DatabaseConnectionFactory;
import eu.drus.test.persistence.core.dbunit.DbFeatureFactory;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

public class EvaluationRule implements MethodRule {

    private final DatabaseConnectionFactory connectionFactory;
    private FeatureResolverFactory featureResolverFactory;

    public EvaluationRule(final FeatureResolverFactory featureResolverFactory, final EntityManagerFactory entityManagerFactory) {
        this.featureResolverFactory = featureResolverFactory;
        final EntityManager tmp = entityManagerFactory.createEntityManager();
        connectionFactory = new DatabaseConnectionFactory(tmp.getProperties());
        tmp.close();
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver featureResolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        try {
            return new EvaluationStatement(connectionFactory, new DbFeatureFactory(featureResolver), base);
        } catch (final IOException e) {
            throw new JpaTestException("Failed to create statement", e);
        }
    }
}
