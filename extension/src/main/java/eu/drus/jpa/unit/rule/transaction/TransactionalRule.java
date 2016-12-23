package eu.drus.jpa.unit.rule.transaction;

import java.lang.reflect.Field;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;

public class TransactionalRule implements MethodRule {

    private FeatureResolverFactory featureResolverFactory;
    private Field persistenceField;

    public TransactionalRule(final FeatureResolverFactory featureResolverFactory, final Field persistenceField) {
        this.featureResolverFactory = featureResolverFactory;
        this.persistenceField = persistenceField;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new TransactionalStatement(featureResolverFactory, persistenceField, base, method, target);
    }
}
