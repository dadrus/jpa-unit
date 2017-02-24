package eu.drus.jpa.unit.rule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;

public interface ExecutionContext {

    Field getPersistenceField();

    FeatureResolver createFeatureResolver(final Method testMethod, final Class<?> clazz);

    DataSource getDataSource();

    IDatabaseConnection openConnection();

    void storeData(String key, Object value);

    Object getData(String key);
}
