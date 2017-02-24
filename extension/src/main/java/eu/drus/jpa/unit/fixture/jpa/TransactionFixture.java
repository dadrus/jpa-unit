package eu.drus.jpa.unit.fixture.jpa;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.fixture.spi.TestFixture;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

public class TransactionFixture implements TestFixture {

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        final FeatureResolver featureResolver = invocation.getContext().createFeatureResolver(invocation.getMethod(),
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
