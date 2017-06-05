package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class SecondLevelCacheDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 0;
    }

    private void evictCache(final boolean doEvict, final EntityManagerFactory emf) {
        if (doEvict) {
            emf.getCache().evictAll();
        }
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final FeatureResolver resolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(), invocation.getTestClass());

        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext()
                .getData(ExecutionContext.KEY_ENTITY_MANAGER_FACTORY);

        evictCache(resolver.shouldEvictCacheBefore(), emf);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final FeatureResolver resolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(), invocation.getTestClass());

        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext()
                .getData(ExecutionContext.KEY_ENTITY_MANAGER_FACTORY);

        evictCache(resolver.shouldEvictCacheAfter(), emf);
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
