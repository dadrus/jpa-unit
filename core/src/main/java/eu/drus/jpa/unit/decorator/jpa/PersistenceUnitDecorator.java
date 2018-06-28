package eu.drus.jpa.unit.decorator.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class PersistenceUnitDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void beforeTest(final TestInvocation invocation) throws Exception {
        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext().getData(Constants.KEY_ENTITY_MANAGER_FACTORY);

        final Class<?> fieldType = invocation.getContext().getPersistenceField().getType();
        if (fieldType.equals(EntityManagerFactory.class)) {
            injectValue(invocation.getTestInstance().get(), invocation.getContext().getPersistenceField(), emf);
        }
    }

    @Override
    public void afterTest(final TestInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
