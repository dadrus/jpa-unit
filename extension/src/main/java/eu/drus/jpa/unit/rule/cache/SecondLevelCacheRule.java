package eu.drus.jpa.unit.rule.cache;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.rule.context.EntityManagerFactoryProducer;

public class SecondLevelCacheRule implements MethodRule {

    private final FeatureResolverFactory featureResolverFactory;
    private EntityManagerFactoryProducer emfProducer;

    public SecondLevelCacheRule(final FeatureResolverFactory featureResolverFactory, final EntityManagerFactoryProducer emfProducer) {
        this.featureResolverFactory = featureResolverFactory;
        this.emfProducer = emfProducer;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver resolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        return new SecondLevelCacheStatement(resolver, emfProducer, base);
    }

}
