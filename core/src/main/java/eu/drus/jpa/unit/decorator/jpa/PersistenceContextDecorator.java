package eu.drus.jpa.unit.decorator.jpa;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class PersistenceContextDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void beforeTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final EntityManagerFactory emf = (EntityManagerFactory) context.getData(Constants.KEY_ENTITY_MANAGER_FACTORY);

        final Field field = context.getPersistenceField();
        if (field.getType().equals(EntityManager.class)) {
            final EntityManager em = getEntityManager(context, emf);
            context.storeData(Constants.KEY_ENTITY_MANAGER, em);
            injectValue(invocation.getTestInstance().get(), field, em);
        }
    }

    private EntityManager getEntityManager(final ExecutionContext context, final EntityManagerFactory emf) {
        EntityManager em = null;
        if (getPersistenceContextType(context) == PersistenceContextType.EXTENDED) {
            // EntityManager may be already open. If so use it.
            em = (EntityManager) context.getData(Constants.KEY_ENTITY_MANAGER);
        }

        if (em == null) {
            // create EntityManager and inject it
            em = emf.createEntityManager();
        }
        return em;
    }

    @Override
    public void afterTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final EntityManager em = (EntityManager) context.getData(Constants.KEY_ENTITY_MANAGER);
        if (em != null && getPersistenceContextType(context) != PersistenceContextType.EXTENDED) {
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
