package eu.drus.jpa.unit.spi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;

public interface ExecutionContext {

    Field getPersistenceField();

    FeatureResolver createFeatureResolver(final Method testMethod, final Class<?> clazz);

    void storeData(String key, Object value);

    Object getData(String key);
}
