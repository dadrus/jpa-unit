package eu.drus.jpa.unit.decorator.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class PersistenceUnitDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext()
                .getData(ExecutionContext.KEY_ENTITY_MANAGER_FACTORY);

        final Class<?> fieldType = invocation.getContext().getPersistenceField().getType();
        if (fieldType.equals(EntityManagerFactory.class)) {
            injectValue(invocation.getContext().getPersistenceField(), instance, emf);
        }
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }

}
