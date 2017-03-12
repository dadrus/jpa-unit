package eu.drus.jpa.unit.spi;

import java.lang.reflect.Field;

public interface ExecutionContext {

    Field getPersistenceField();

    void storeData(String key, Object value);

    Object getData(String key);
}
