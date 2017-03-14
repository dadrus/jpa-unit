package eu.drus.jpa.unit.cdi;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class CdiProducerDecorator implements TestMethodDecorator {

    private EntityManagerHolder emh;

    public CdiProducerDecorator() {
        emh = EntityManagerHolder.INSTANCE;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void apply(final TestMethodInvocation invocation) throws Throwable {
        try {
            final EntityManager em = (EntityManager) invocation.getContext().getData("em");
            emh.setEntityManager(em);
            invocation.proceed();
        } finally {
            emh.setEntityManager(null);
        }
    }
}
