package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class TransactionDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public void apply(final TestMethodInvocation invocation) throws Throwable {
        final FeatureResolver featureResolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(),
                invocation.getTarget().getClass());

        final EntityManager em = (EntityManager) invocation.getContext().getData("em");

        if (em == null) {
            invocation.proceed();
        } else {
            try {
                final TransactionStrategyExecutor executor = featureResolver.getTransactionMode()
                        .provide(new TransactionStrategyProvider(em.getTransaction()));
                executor.execute(invocation);
            } finally {
                em.clear();
            }
        }
    }

}
