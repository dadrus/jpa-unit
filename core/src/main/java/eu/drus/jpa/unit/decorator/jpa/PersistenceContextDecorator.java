package eu.drus.jpa.unit.decorator.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class PersistenceContextDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final EntityManagerFactory emf = (EntityManagerFactory) context.getData("emf");
        EntityManager em;

        final Field field = context.getPersistenceField();
        if (field.getType().equals(EntityManager.class)) {
            // create EntityManager and inject it
            em = emf.createEntityManager();
            injectValue(field, instance, em);
            context.storeData("em", em);
        }
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final EntityManager em = (EntityManager) context.getData("em");
        if (em != null) {
            context.storeData("em", null);
            em.close();
        }
    }
}
