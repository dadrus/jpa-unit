package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class EntityManagerFactoryDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(descriptor.getUnitName(), descriptor.getProperties());
        ctx.storeData(Constants.KEY_ENTITY_MANAGER_FACTORY, emf);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final EntityManagerFactory emf = (EntityManagerFactory) ctx.getData(Constants.KEY_ENTITY_MANAGER_FACTORY);
        ctx.storeData(Constants.KEY_ENTITY_MANAGER_FACTORY, null);
        emf.close();
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
