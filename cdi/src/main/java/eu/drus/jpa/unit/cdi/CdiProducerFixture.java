package eu.drus.jpa.unit.cdi;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.fixture.spi.TestFixture;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

public class CdiProducerFixture implements TestFixture {

    private EntityManagerHolder emh;

    public CdiProducerFixture() {
        emh = EntityManagerHolder.INSTANCE;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        try {
            final EntityManager em = (EntityManager) invocation.getContext().getData("em");
            emh.setEntityManager(em);
            invocation.proceed();
        } finally {
            emh.setEntityManager(null);
        }
    }
}
