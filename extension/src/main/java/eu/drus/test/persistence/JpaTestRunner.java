package eu.drus.test.persistence;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import org.junit.rules.MethodRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import eu.drus.test.persistence.core.PersistenceUnitDescriptor;
import eu.drus.test.persistence.core.PersistenceUnitDescriptorLoader;
import eu.drus.test.persistence.core.metadata.AnnotationInspector;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;
import eu.drus.test.persistence.core.metadata.MetadataExtractor;
import eu.drus.test.persistence.rule.context.PersistenceContextRule;
import eu.drus.test.persistence.rule.evaluation.EvaluationRule;
import eu.drus.test.persistence.rule.transaction.TransactionalRule;

public class JpaTestRunner extends BlockJUnit4ClassRunner {

    private EntityManagerFactory entityManagerFactory;
    private Field persistenceField;
    private Map<String, Object> properties;

    public JpaTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    private static Map<String, Object> getPersistenceContextProperties(final PersistenceContext persistenceContext) {
        final Map<String, Object> properties = new HashMap<>();
        for (final PersistenceProperty property : persistenceContext.properties()) {
            properties.put(property.name(), property.value());
        }
        return properties;
    }

    @Override
    protected List<MethodRule> rules(final Object target) {
        final List<MethodRule> rules = super.rules(target);
        rules.addAll(getMethodRules());
        return rules;
    }

    private List<MethodRule> getMethodRules() {
        final FeatureResolverFactory featureResolverFactory = new FeatureResolverFactory();
        final List<MethodRule> rules = new ArrayList<>();
        rules.add(new TransactionalRule(featureResolverFactory, persistenceField));
        rules.add(new EvaluationRule(featureResolverFactory, properties));
        rules.add(new PersistenceContextRule(entityManagerFactory, persistenceField));
        return rules;
    }

    @Override
    public void run(final RunNotifier notifier) {
        try {
            final MetadataExtractor extractor = new MetadataExtractor(getTestClass());
            final AnnotationInspector<PersistenceContext> pcInspector = extractor.persistenceContext();
            final AnnotationInspector<PersistenceUnit> puInspector = extractor.persistenceUnit();
            final List<Field> pcFields = pcInspector.getAnnotatedFields();
            final List<Field> puFields = puInspector.getAnnotatedFields();

            checkArgument(!(puFields.isEmpty() && pcFields.isEmpty()),
                    "JPA test must have either EntityManagerFactory or EntityManager field annotated with @PersistenceUnit, respectively @PersistenceContext");

            checkArgument(!(!puFields.isEmpty() && !pcFields.isEmpty()),
                    "Only single field annotated with either @PersistenceUnit or @PersistenceContext is allowed to be present");

            checkArgument(puFields.size() <= 1, "Only single field is allowed to be annotated with @PersistenceUnit");

            checkArgument(pcFields.size() <= 1, "Only single field is allowed to be annotated with @PersistenceContext");

            String unitName;

            if (!puFields.isEmpty()) {
                persistenceField = puFields.get(0);
                checkArgument(persistenceField.getType().equals(EntityManagerFactory.class), String.format(
                        "Field %s annotated with @PersistenceUnit is not of type EntityManagerFactory.", persistenceField.getName()));
                final PersistenceUnit persistenceUnit = puInspector.fetchFromField(persistenceField);
                unitName = persistenceUnit.unitName();
                properties = Collections.emptyMap();
            } else {
                persistenceField = pcFields.get(0);
                checkArgument(persistenceField.getType().equals(EntityManager.class), String
                        .format("Field %s annotated with @PersistenceContext is not of type EntityManager.", persistenceField.getName()));
                final PersistenceContext persistenceContext = pcInspector.fetchFromField(persistenceField);
                unitName = persistenceContext.unitName();
                properties = getPersistenceContextProperties(persistenceContext);
            }

            entityManagerFactory = Persistence.createEntityManagerFactory(unitName, properties);

            final PersistenceUnitDescriptorLoader pudLoader = new PersistenceUnitDescriptorLoader();
            List<PersistenceUnitDescriptor> descriptors = pudLoader.loadPersistenceUnitDescriptors(properties);

            descriptors = descriptors.stream().filter(u -> unitName.equals(u.getUnitName())).collect(Collectors.toList());

            if (descriptors.isEmpty()) {
                throw new JpaTestException("No peristence unit found for given unit name");
            }
            if (descriptors.size() > 1) {
                throw new JpaTestException("Multiple persistence units found for given name");
            }

            properties = descriptors.get(0).getProperties();

            super.run(notifier);
        } catch (final IOException e) {
            throw new JpaTestException("Error while loading persistence unit descriptors", e);
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    private static void checkArgument(final boolean flag, final String msg) {
        if (!flag) {
            throw new IllegalArgumentException(msg);
        }
    }

}
