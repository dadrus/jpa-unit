package eu.drus.jpa.unit.rule.cache;

import javax.persistence.EntityManagerFactory;

import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;

public class SecondLevelCacheStatement extends Statement {

    private final FeatureResolver resolver;
    private final ExecutionContext ctx;
    private Statement base;

    public SecondLevelCacheStatement(final FeatureResolver resolver, final ExecutionContext ctx, final Statement base) {
        this.resolver = resolver;
        this.ctx = ctx;
        this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
        final EntityManagerFactory emf = ctx.createEntityManagerFactory();

        try {
            doEvaluate(emf);
        } finally {
            ctx.destroyEntityManagerFactory(emf);
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
