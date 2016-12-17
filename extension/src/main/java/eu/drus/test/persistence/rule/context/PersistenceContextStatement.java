package eu.drus.test.persistence.rule.context;

import static eu.drus.test.persistence.util.ReflectionUtils.injectValue;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.metadata.FeatureResolver;

public class PersistenceContextStatement extends Statement {

    private final FeatureResolver resolver;
    private final EntityManagerFactoryProducer emfProducer;
    private final Field persistenceField;
    private final Statement base;
    private final Object target;

    public PersistenceContextStatement(final FeatureResolver resolver, final EntityManagerFactoryProducer emfProducer,
            final Field persistenceField, final Statement base, final Object target) {
        this.resolver = resolver;
        this.emfProducer = emfProducer;
        this.persistenceField = persistenceField;
        this.base = base;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        final EntityManagerFactory emf = emfProducer.createEntityManagerFactory();

        try {
            doEvaluate(emf);
        } finally {
            emfProducer.destroyEntityManagerFactory(emf);
        }
    }

    private void doEvaluate(final EntityManagerFactory emf) throws Throwable {
        EntityManager em = null;

        final Class<?> fieldType = persistenceField.getType();
        if (fieldType.equals(EntityManagerFactory.class)) {
            // just inject the factory
            injectValue(persistenceField, target, emf);
        } else if (fieldType.equals(EntityManager.class)) {
            // create EntityManager and inject it
            em = emf.createEntityManager();
            injectValue(persistenceField, target, em);
        } else {
            throw new IllegalArgumentException("Unexpected field type: " + fieldType.getName());
        }

        evictCache(resolver.shouldCleanupBefore() && resolver.shouldEvictCache(), emf);

        try {
            base.evaluate();
        } finally {
            clearSecondLevelCacheAndCloseEntityManager(emf, em);
        }
    }

    private void clearSecondLevelCacheAndCloseEntityManager(final EntityManagerFactory emf, final EntityManager em) {
        if (em != null) {
            try {
                em.close();
            } catch (final IllegalStateException e) {
                // TODO: log warning
            } finally {
                evictCache(resolver.shouldCleanupAfter() && resolver.shouldEvictCache(), emf);
            }
        }
    }

    private void evictCache(final boolean doEvict, final EntityManagerFactory emf) {
        if (doEvict) {
            emf.getCache().evictAll();
        }
    }

}
