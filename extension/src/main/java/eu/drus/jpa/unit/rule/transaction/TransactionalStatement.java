package eu.drus.jpa.unit.rule.transaction;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;

public class TransactionalStatement extends Statement {
    private final Field persistenceField;
    private final FeatureResolver featureResolver;
    private Statement base;
    private Object target;

    public TransactionalStatement(final ExecutionContext ctx, final Statement base, final FrameworkMethod method, final Object target) {
        persistenceField = ctx.getPersistenceField();
        this.base = base;
        this.target = target;
        featureResolver = ctx.createFeatureResolver(method.getMethod(), target.getClass());
    }

    @Override
    public void evaluate() throws Throwable {

        final EntityManager entityManager = getEntityManager(target);

        if (entityManager == null) {
            base.evaluate();
        } else {
            evaluateInTransaction(base, entityManager);
        }
    }

    private void evaluateInTransaction(final Statement base, final EntityManager entityManager) throws Throwable {
        try {
            final TransactionStrategyExecutor executor = featureResolver.getTransactionMode()
                    .provide(new TransactionStrategyProvider(entityManager.getTransaction()));
            executor.execute(base);
        } finally {
            entityManager.clear();
        }
    }

    private EntityManager getEntityManager(final Object target) throws IllegalAccessException {
        EntityManager entityManager = null;
        if (persistenceField.getType().equals(EntityManager.class)) {
            final boolean isAccessible = persistenceField.isAccessible();
            persistenceField.setAccessible(true);
            entityManager = (EntityManager) persistenceField.get(target);
            persistenceField.setAccessible(isAccessible);
        }
        return entityManager;
    }
}
