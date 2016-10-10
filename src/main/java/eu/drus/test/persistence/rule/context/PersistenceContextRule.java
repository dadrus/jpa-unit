package eu.drus.test.persistence.rule.context;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class PersistenceContextRule implements MethodRule {

    private final EntityManagerFactory entityManagerFactory;
    private final Field persistenceField;

    public PersistenceContextRule(final EntityManagerFactory entityManagerFactory, final Field persistenceField) {
        this.entityManagerFactory = entityManagerFactory;
        this.persistenceField = persistenceField;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new PersistenceContextStatement(entityManagerFactory, persistenceField, base, target);
    }

}
