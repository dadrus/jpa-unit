package eu.drus.jpa.unit.core;

import static eu.drus.jpa.unit.util.Preconditions.checkArgument;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.metadata.AnnotationInspector;
import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.spi.ExecutionContext;

public class JpaUnitContext implements ExecutionContext {

    private static final Map<Class<?>, JpaUnitContext> CTX_MAP = new HashMap<>();

    private Field persistenceField;

    private Map<String, Object> cache;

    private JpaUnitContext(final Class<?> testClass) {
        final MetadataExtractor extractor = new MetadataExtractor(testClass);
        final AnnotationInspector<PersistenceContext> pcInspector = extractor.persistenceContext();
        final AnnotationInspector<PersistenceUnit> puInspector = extractor.persistenceUnit();
        final List<Field> pcFields = pcInspector.getAnnotatedFields();
        final List<Field> puFields = puInspector.getAnnotatedFields();

        checkArgument(!puFields.isEmpty() || !pcFields.isEmpty(),
                "JPA test must have either EntityManagerFactory or EntityManager field annotated with @PersistenceUnit, respectively @PersistenceContext");

        checkArgument(puFields.isEmpty() || pcFields.isEmpty(),
                "Only single field annotated with either @PersistenceUnit or @PersistenceContext is allowed to be present");

        checkArgument(puFields.size() <= 1, "Only single field is allowed to be annotated with @PersistenceUnit");

        checkArgument(pcFields.size() <= 1, "Only single field is allowed to be annotated with @PersistenceContext");

        String unitName;
        Map<String, Object> properties;

        if (!puFields.isEmpty()) {
            persistenceField = puFields.get(0);
            checkArgument(persistenceField.getType().equals(EntityManagerFactory.class), String
                    .format("Field %s annotated with @PersistenceUnit is not of type EntityManagerFactory.", persistenceField.getName()));
            final PersistenceUnit persistenceUnit = puInspector.fetchFromField(persistenceField);
            unitName = persistenceUnit.unitName();
            properties = Collections.emptyMap();
        } else {
            persistenceField = pcFields.get(0);
            checkArgument(persistenceField.getType().equals(EntityManager.class),
                    String.format("Field %s annotated with @PersistenceContext is not of type EntityManager.", persistenceField.getName()));
            final PersistenceContext persistenceContext = pcInspector.fetchFromField(persistenceField);
            unitName = persistenceContext.unitName();
            properties = getPersistenceContextProperties(persistenceContext);
        }

        if (unitName == null || unitName.isEmpty()) {
            throw new JpaUnitException("No Persistence Unit found for given unit name");
        }

        cache = new HashMap<>();
        cache.put("unitName", unitName);
        cache.put("properties", properties);
    }

    public static synchronized JpaUnitContext getInstance(final Class<?> testClass) {
        JpaUnitContext ctx = CTX_MAP.get(testClass);
        if (ctx == null) {
            ctx = new JpaUnitContext(testClass);
            CTX_MAP.put(testClass, ctx);
        }
        return ctx;
    }

    private static Map<String, Object> getPersistenceContextProperties(final PersistenceContext persistenceContext) {
        final Map<String, Object> properties = new HashMap<>();
        for (final PersistenceProperty property : persistenceContext.properties()) {
            properties.put(property.name(), property.value());
        }
        return properties;
    }

    @Override
    public Field getPersistenceField() {
        return persistenceField;
    }

    @Override
    public void storeData(final String key, final Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getData(final String key) {
        return cache.get(key);
    }
}
