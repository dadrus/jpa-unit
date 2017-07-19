package eu.drus.jpa.unit.cdi;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
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
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final EntityManager em = (EntityManager) invocation.getContext().getData(Constants.KEY_ENTITY_MANAGER);
        emh.setEntityManager(em);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        emh.setEntityManager(null);
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }
}
