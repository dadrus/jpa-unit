package eu.drus.jpa.unit.decorator.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import eu.drus.jpa.unit.spi.Constants;
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

        final EntityManagerFactory emf = (EntityManagerFactory) context.getData(Constants.KEY_ENTITY_MANAGER_FACTORY);
        EntityManager em;

        final Field field = context.getPersistenceField();
        if (field.getType().equals(EntityManager.class)) {
            // create EntityManager and inject it
            em = emf.createEntityManager();
            injectValue(field, instance, em);
            context.storeData(Constants.KEY_ENTITY_MANAGER, em);
        }
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final EntityManager em = (EntityManager) context.getData(Constants.KEY_ENTITY_MANAGER);
        if (em != null && getPersistenceContextType(context) == PersistenceContextType.TRANSACTION) {
            context.storeData(Constants.KEY_ENTITY_MANAGER, null);
            em.close();
        }
    }

    private PersistenceContextType getPersistenceContextType(final ExecutionContext context) {
        final Field field = context.getPersistenceField();
        final PersistenceContext pc = field.getAnnotation(PersistenceContext.class);
        return pc.type();
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return true;
    }
}
