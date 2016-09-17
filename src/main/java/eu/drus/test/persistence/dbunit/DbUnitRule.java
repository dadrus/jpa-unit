package eu.drus.test.persistence.dbunit;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

public class DbUnitRule implements MethodRule {

    private final Map<String, Object> properties;
    private FeatureResolverFactory featureResolverFactory;

    public DbUnitRule(final FeatureResolverFactory featureResolverFactory, final EntityManagerFactory entityManagerFactory) {
        this.featureResolverFactory = featureResolverFactory;
        final EntityManager tmp = entityManagerFactory.createEntityManager();
        properties = new HashMap<>();
        properties.putAll(tmp.getProperties());
        tmp.close();
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver featureResolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        return new DbUnitStatement(properties, featureResolver, base);
    }
}
