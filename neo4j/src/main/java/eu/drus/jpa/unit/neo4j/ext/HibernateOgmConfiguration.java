package eu.drus.jpa.unit.neo4j.ext;

import java.io.IOException;
import java.util.Map;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class HibernateOgmConfiguration extends AbstractConfiguration {

    protected static final String HIBERNATE_OGM_DATASTORE_PASSWORD = "hibernate.ogm.datastore.password";
    protected static final String HIBERNATE_OGM_DATASTORE_USERNAME = "hibernate.ogm.datastore.username";
    protected static final String HIBERNATE_OGM_DATASTORE_HOST = "hibernate.ogm.datastore.host";
    protected static final String HIBERNATE_OGM_DATASTORE_PROVIDER = "hibernate.ogm.datastore.provider";
    protected static final String HIBERNATE_OGM_DATABASE_PATH = "hibernate.ogm.neo4j.database_path";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> dbConfig = descriptor.getProperties();

            final String provider = (String) dbConfig.get(HIBERNATE_OGM_DATASTORE_PROVIDER);

            return provider != null
                    && (provider.equals("neo4j_bolt") || provider.equals("neo4j_http") || provider.equals("neo4j_embedded"));
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            try {
                return new HibernateOgmConfiguration(descriptor);
            } catch (final IOException e) {
                throw new JpaUnitException("Could not create HibernateOgmConfiguration", e);
            }
        }
    }

    private HibernateOgmConfiguration(final PersistenceUnitDescriptor descriptor) throws IOException {
        final Map<String, Object> properties = descriptor.getProperties();

        final String host = (String) properties.get(HIBERNATE_OGM_DATASTORE_HOST);
        username = (String) properties.get(HIBERNATE_OGM_DATASTORE_USERNAME);
        password = (String) properties.get(HIBERNATE_OGM_DATASTORE_PASSWORD);

        final String provider = (String) properties.get(HIBERNATE_OGM_DATASTORE_PROVIDER);
        if (provider.equals("neo4j_bolt")) {
            connectionUrl = "jdbc:neo4j:bolt://" + host;
        } else if (provider.equals("neo4j_http")) {
            connectionUrl = "jdbc:neo4j:http://" + host;
        } else if (provider.equals("neo4j_embedded")) {
            loadConfigurationForEmbeddedSetup();
        } else {
            throw new JpaUnitException("Unsupported provider configuration for Hibernate OGM: " + provider);
        }
    }

}
