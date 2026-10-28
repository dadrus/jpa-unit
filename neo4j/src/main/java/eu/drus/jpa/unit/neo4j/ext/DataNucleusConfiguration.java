package eu.drus.jpa.unit.neo4j.ext;

import java.io.IOException;
import java.util.Map;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceProperties;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class DataNucleusConfiguration extends AbstractConfiguration {

    protected static final String DATANUCLEUS_CONNECTION_URL = "datanucleus.ConnectionURL";
    private static final String JDBC_URL_SUFFIX = "jdbc.url";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> properties = descriptor.getProperties();
            final String url = PersistenceProperties.containsKey(properties, JDBC_URL_SUFFIX)
                    ? (String) PersistenceProperties.get(properties, JDBC_URL_SUFFIX)
                    : (String) properties.get(DATANUCLEUS_CONNECTION_URL);

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
