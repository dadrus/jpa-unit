package eu.drus.jpa.unit.spi;

import java.util.List;
import java.util.Map;

public interface PersistenceUnitDescriptor {
    String getUnitName();

    String getProviderClassName();

    Map<String, Object> getProperties();

    List<Class<?>> getClasses();
}
