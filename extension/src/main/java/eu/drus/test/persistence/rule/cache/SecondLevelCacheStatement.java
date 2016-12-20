package eu.drus.test.persistence.rule.cache;

import javax.persistence.EntityManagerFactory;

import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.rule.context.EntityManagerFactoryProducer;

public class SecondLevelCacheStatement extends Statement {

    private final FeatureResolver resolver;
    private final EntityManagerFactoryProducer emfProducer;
    private Statement base;

    public SecondLevelCacheStatement(final FeatureResolver resolver, final EntityManagerFactoryProducer emfProducer, final Statement base) {
        this.resolver = resolver;
        this.emfProducer = emfProducer;
        this.base = base;
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
        evictCache(resolver.shouldEvictCacheBefore(), emf);

        try {
            base.evaluate();
        } finally {
            evictCache(resolver.shouldEvictCacheAfter(), emf);
        }
    }

    private void evictCache(final boolean doEvict, final EntityManagerFactory emf) {
        if (doEvict) {
            emf.getCache().evictAll();
        }
    }

}
