package eu.drus.test.persistence.rule.transaction;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

public class TransactionalRule implements MethodRule {

    private FeatureResolverFactory featureResolverFactory;
    private Field persistenceField;

    public TransactionalRule(final FeatureResolverFactory featureResolverFactory, final Field persistenceField) {
        this.featureResolverFactory = featureResolverFactory;
        this.persistenceField = persistenceField;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            private final FeatureResolver featureResolver = featureResolverFactory.createFeatureResolver(method.getMethod(),
                    target.getClass());

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
        };
    }
}
