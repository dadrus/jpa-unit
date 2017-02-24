package eu.drus.jpa.unit.fixture.jpa;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.fixture.spi.TestFixture;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

public class SecondLevelCacheFixture implements TestFixture {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        final FeatureResolver resolver = invocation.getContext().createFeatureResolver(invocation.getMethod(),
                invocation.getTarget().getClass());

        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext().getData("emf");

        evictCache(resolver.shouldEvictCacheBefore(), emf);

        try {
            invocation.proceed();
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
