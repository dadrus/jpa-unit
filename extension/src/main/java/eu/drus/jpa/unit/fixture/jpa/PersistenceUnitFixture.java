package eu.drus.jpa.unit.fixture.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.fixture.spi.TestFixture;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

public class PersistenceUnitFixture implements TestFixture {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        final EntityManagerFactory emf = (EntityManagerFactory) invocation.getContext().getData("emf");

        final Class<?> fieldType = invocation.getContext().getPersistenceField().getType();
        if (fieldType.equals(EntityManagerFactory.class)) {
            injectValue(invocation.getContext().getPersistenceField(), invocation.getTarget(), emf);
        }

        invocation.proceed();
    }

}
