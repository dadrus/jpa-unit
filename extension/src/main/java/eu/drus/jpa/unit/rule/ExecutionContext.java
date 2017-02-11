package eu.drus.jpa.unit.rule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;

public interface ExecutionContext {

    EntityManagerFactory createEntityManagerFactory();

    void destroyEntityManagerFactory(final EntityManagerFactory emf);

    Field getPersistenceField();

    Map<String, Object> getDataBaseConnectionProperties();

    FeatureResolver createFeatureResolver(final Method testMethod, final Class<?> clazz);
}
