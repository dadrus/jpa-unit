package eu.drus.test.persistence.context;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
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

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                EntityManager entityManager = null;

                final Class<?> persistenceContextFieldType = persistenceField.getType();
                final boolean isAccessible = persistenceField.isAccessible();
                persistenceField.setAccessible(true);
                try {
                    if (persistenceContextFieldType.equals(EntityManagerFactory.class)) {
                        // just inject the factory
                        persistenceField.set(target, entityManagerFactory);
                    } else if (persistenceContextFieldType.equals(EntityManager.class)) {
                        // create EntityManager and inject it
                        entityManager = entityManagerFactory.createEntityManager();
                        persistenceField.set(target, entityManager);
                    }
                } finally {
                    persistenceField.setAccessible(isAccessible);
                }

                try {
                    base.evaluate();
                } finally {
                    if (entityManager != null) {
                        entityManager.close();
                    }
                }
            }
        };
    }

}
