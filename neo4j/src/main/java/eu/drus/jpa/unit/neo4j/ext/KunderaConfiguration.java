package eu.drus.jpa.unit.neo4j.ext;

import java.io.IOException;
import java.util.Map;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class KunderaConfiguration extends AbstractConfiguration {

    protected static final String KUNDERA_DIALECT = "kundera.dialect";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> properties = descriptor.getProperties();

            final String dialect = (String) properties.get(KUNDERA_DIALECT);
            return dialect != null && dialect.contains("neo4j");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            try {
                return new KunderaConfiguration();
            } catch (final IOException e) {
                throw new JpaUnitException("Could not create KunderaConfiguration", e);
            }
        }
    }

    private KunderaConfiguration() throws IOException {
        loadConfigurationForEmbeddedSetup();
    }
}
