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
    public void apply(final TestMethodInvocation invocation) throws Throwable {
        final ExecutionContext context = invocation.getContext();

        final EntityManagerFactory emf = (EntityManagerFactory) context.getData("emf");
        EntityManager em = null;

        final Field field = context.getPersistenceField();

        try {
            if (field.getType().equals(EntityManager.class)) {
                // create EntityManager and inject it
                em = emf.createEntityManager();
                injectValue(field, invocation.getTarget(), em);
                context.storeData("em", em);
            }

            invocation.proceed();
        } finally {
            if (em != null) {
                context.storeData("em", null);
                em.close();
            }
        }
    }
}
