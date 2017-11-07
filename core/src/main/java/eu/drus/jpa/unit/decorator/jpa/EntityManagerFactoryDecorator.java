package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;

public class EntityManagerFactoryDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void beforeAll(final TestInvocation invocation) throws Exception {
        ExecutionContext context = invocation.getContext();
        final PersistenceUnitDescriptor descriptor = context.getDescriptor();

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(descriptor.getUnitName(), descriptor.getProperties());
        context.storeData(Constants.KEY_ENTITY_MANAGER_FACTORY, emf);
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        // if EntityManager has been configured for EXTENDED transaction mode, same instance is used
        // throughout all the tests/test steps. In that case we have to close it here, before the
        // EntityManagerFactory is closed.
        ExecutionContext context = invocation.getContext();
        final EntityManager em = (EntityManager) context.getData(Constants.KEY_ENTITY_MANAGER);
        context.storeData(Constants.KEY_ENTITY_MANAGER, null);
        if (em != null) {
            em.close();
        }

        final EntityManagerFactory emf = (EntityManagerFactory) context.getData(Constants.KEY_ENTITY_MANAGER_FACTORY);
        context.storeData(Constants.KEY_ENTITY_MANAGER_FACTORY, null);
        emf.close();
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
