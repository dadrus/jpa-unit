package eu.drus.jpa.unit.spi;

import java.util.Map;

public final class PersistenceProperties {

    private static final String JAKARTA_PERSISTENCE_PREFIX = "jakarta.persistence.";
    private static final String JAVAX_PERSISTENCE_PREFIX = "javax.persistence.";

    private PersistenceProperties() {
    }

    public static boolean containsKey(final Map<String, Object> properties, final String suffix) {
        return properties.containsKey(JAKARTA_PERSISTENCE_PREFIX + suffix) || properties.containsKey(JAVAX_PERSISTENCE_PREFIX + suffix);
    }

    public static Object get(final Map<String, Object> properties, final String suffix) {
        final Object value = properties.get(JAKARTA_PERSISTENCE_PREFIX + suffix);
        return value != null ? value : properties.get(JAVAX_PERSISTENCE_PREFIX + suffix);
    }
}
