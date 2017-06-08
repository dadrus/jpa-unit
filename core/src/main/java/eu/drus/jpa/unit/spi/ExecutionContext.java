package eu.drus.jpa.unit.spi;

import java.lang.reflect.Field;

public interface ExecutionContext {

    static final String KEY_ENTITY_MANAGER_FACTORY = "emf";
    static final String KEY_ENTITY_MANAGER = "em";

    Field getPersistenceField();

    PersistenceUnitDescriptor getDescriptor();

    void storeData(String key, Object value);

    Object getData(String key);
}
