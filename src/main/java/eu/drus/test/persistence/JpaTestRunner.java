package eu.drus.test.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;

import org.junit.rules.MethodRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import eu.drus.test.persistence.context.PersistenceContextRule;
import eu.drus.test.persistence.core.metadata.AnnotationInspector;
import eu.drus.test.persistence.core.metadata.MetadataExtractor;
import eu.drus.test.persistence.dbunit.DbUnitRule;
import eu.drus.test.persistence.transaction.TransactionalRule;

public class JpaTestRunner extends BlockJUnit4ClassRunner {

    private EntityManagerFactory entityManagerFactory;
    private Field persistenceField;

    public JpaTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object target) {
        final List<MethodRule> rules = super.rules(target);
        rules.add(new TransactionalRule());
        rules.add(new DbUnitRule(entityManagerFactory));
        rules.add(new PersistenceContextRule(entityManagerFactory, persistenceField));
        return rules;
    }

    @Override
    public void run(final RunNotifier notifier) {
        try {
            final MetadataExtractor extractor = new MetadataExtractor(getTestClass());
            final AnnotationInspector<PersistenceContext> inspector = extractor.persistenceContext();
            final List<Field> fields = inspector.getAnnotatedFields();

            if (fields.isEmpty()) {
                throw new IllegalArgumentException(
                        "JPA test must have either EntityManagerFactory or EntityManager field annotated with @PersistenceContext");
            }

            if (fields.size() > 1) {
                throw new IllegalArgumentException("Only single field is allowed to be annotated with @PersistenceContext");
            }

            persistenceField = fields.get(0);
            if (!persistenceField.getType().equals(EntityManagerFactory.class) && !persistenceField.getType().equals(EntityManager.class)) {
                throw new IllegalArgumentException(String.format(
                        "Filed %s annotated with @PersistenceContext is neither of type EntityManagerFactory, nor EntityManager.",
                        persistenceField.getName()));
            }

            final PersistenceContext persistenceContext = inspector.fetchFromField(persistenceField);
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceContext.unitName(),
                    getPersistenceContextProperties(persistenceContext));

            super.run(notifier);
        } finally {
            shutdown();
        }
    }

    private Map<String, String> getPersistenceContextProperties(final PersistenceContext persistenceContext) {
        final Map<String, String> properties = new HashMap<>();
        for (final PersistenceProperty property : persistenceContext.properties()) {
            properties.put(property.name(), property.value());
        }
        return properties;
    }

    private void shutdown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

}
