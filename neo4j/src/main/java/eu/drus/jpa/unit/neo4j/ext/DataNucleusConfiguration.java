package eu.drus.jpa.unit.neo4j.ext;

import java.io.IOException;
import java.util.Map;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class DataNucleusConfiguration extends AbstractConfiguration {

    protected static final String DATANUCLEUS_CONNECTION_URL = "datanucleus.ConnectionURL";
    protected static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        private static Object getProperty(final Map<String, Object> properties, final String name, final String alternativeName) {
            if (properties.containsKey(name)) {
                return properties.get(name);
            } else {
                return properties.get(alternativeName);
            }
        }

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final String url = (String) getProperty(descriptor.getProperties(), JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL);

            return url != null ? url.startsWith("neo4j:") : false;
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            try {
                return new DataNucleusConfiguration();
            } catch (final IOException e) {
                throw new JpaUnitException("Could not create DataNucleusConfiguration", e);
            }
        }
    }

    private DataNucleusConfiguration() throws IOException {
        loadConfigurationForEmbeddedSetup();
    }
}
