package eu.drus.test.persistence.rule.context;

import java.lang.reflect.Field;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

public class PersistenceContextRule implements MethodRule {

    private final FeatureResolverFactory featureResolverFactory;
    private final EntityManagerFactoryProducer emfProducer;
    private final Field persistenceField;

    public PersistenceContextRule(final FeatureResolverFactory featureResolverFactory, final EntityManagerFactoryProducer emfProducer,
            final Field persistenceField) {
        this.featureResolverFactory = featureResolverFactory;
        this.emfProducer = emfProducer;
        this.persistenceField = persistenceField;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver resolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        return new PersistenceContextStatement(resolver, emfProducer, persistenceField, base, target);
    }

}
