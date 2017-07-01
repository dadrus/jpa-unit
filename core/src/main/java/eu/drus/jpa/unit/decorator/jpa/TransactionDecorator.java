package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class TransactionDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final EntityManager em = (EntityManager) invocation.getContext().getData(Constants.KEY_ENTITY_MANAGER);
        if (em == null) {
            return;
        }

        final FeatureResolver featureResolver = new FeatureResolver(invocation.getMethod(), invocation.getTestClass());

        final TransactionStrategyExecutor executor = featureResolver.getTransactionMode()
                .provide(new TransactionStrategyProvider(em.getTransaction()));
        executor.begin();
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final EntityManager em = (EntityManager) invocation.getContext().getData(Constants.KEY_ENTITY_MANAGER);
        if (em == null) {
            return;
        }

        final FeatureResolver featureResolver = new FeatureResolver(invocation.getMethod(), invocation.getTestClass());

        final TransactionStrategyExecutor executor = featureResolver.getTransactionMode()
                .provide(new TransactionStrategyProvider(em.getTransaction()));
        executor.commit();

        em.clear();
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
