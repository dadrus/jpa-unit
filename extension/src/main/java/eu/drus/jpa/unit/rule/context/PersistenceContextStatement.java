package eu.drus.jpa.unit.rule.context;

import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.drus.jpa.unit.rule.ExecutionContext;

public class PersistenceContextStatement extends Statement {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceContextStatement.class);

    private final ExecutionContext ctx;
    private final Statement base;
    private final Object target;

    public PersistenceContextStatement(final ExecutionContext ctx, final Statement base, final Object target) {
        this.ctx = ctx;
        this.base = base;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        final EntityManagerFactory emf = ctx.createEntityManagerFactory();

        try {
            doEvaluate(emf);
        } finally {
            ctx.destroyEntityManagerFactory(emf);
        }
    }

    private void doEvaluate(final EntityManagerFactory emf) throws Throwable {
        EntityManager em = null;

        final Class<?> fieldType = ctx.getPersistenceField().getType();
        if (fieldType.equals(EntityManagerFactory.class)) {
            // just inject the factory
            injectValue(ctx.getPersistenceField(), target, emf);
        } else if (fieldType.equals(EntityManager.class)) {
            // create EntityManager and inject it
            em = emf.createEntityManager();
            injectValue(ctx.getPersistenceField(), target, em);
        } else {
            throw new IllegalArgumentException("Unexpected field type: " + fieldType.getName());
        }

        try {
            base.evaluate();
        } finally {
            closeEntityManager(em);
        }
    }

    private void closeEntityManager(final EntityManager em) {
        if (em != null) {
            try {
                em.close();
            } catch (final Exception e) {
                LOG.error("Unexpected exception while closing EntityManager", e);
            }
        }
    }
}
