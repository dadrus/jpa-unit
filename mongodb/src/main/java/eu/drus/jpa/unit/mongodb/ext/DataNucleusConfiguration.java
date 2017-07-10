package eu.drus.jpa.unit.mongodb.ext;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.net.HostAndPort;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class DataNucleusConfiguration extends AbstractConfiguration {

    private static final String DATANUCLEUS_MONGODB_OPTIONS_PREFIX = "datanucleus.mongodb";
    private static final String DATANUCLEUS_CONNECTION_URL = "datanucleus.ConnectionURL";
    private static final String DATANUCLEUS_CONNECTION_PASSWORD = "datanucleus.ConnectionPassword";
    private static final String DATANUCLEUS_CONNECTION_USER_NAME = "datanucleus.ConnectionUserName";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final String url = (String) getProperty(descriptor.getProperties(), JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL);

            return url != null ? url.startsWith("mongodb:") : false;
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new DataNucleusConfiguration(descriptor);
        }
    }

    private DataNucleusConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddressesAndDatabaseName(properties);
        configureCredentials(properties);
        configureClientOptions(properties);

    }

    private void configureServerAddressesAndDatabaseName(final Map<String, Object> properties) {
        parseUrl((String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL));
        checkArgument(databaseName != null, DATANUCLEUS_CONNECTION_URL + " does not contain a database name part");
    }

    private void configureClientOptions(final Map<String, Object> properties) {
        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        setOptions(builder, (final String key) -> (String) properties.get(DATANUCLEUS_MONGODB_OPTIONS_PREFIX + "." + key));
        mongoClientOptions = builder.build();
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_USER, DATANUCLEUS_CONNECTION_USER_NAME);
        final String password = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_PASSWORD, DATANUCLEUS_CONNECTION_PASSWORD);

        if (userName != null) {
            checkArgument(password != null, "neither " + JAVAX_PERSISTENCE_JDBC_PASSWORD + ", nor " + DATANUCLEUS_CONNECTION_PASSWORD
                    + " were configured, but required");
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, "admin", password.toCharArray()));
        } else {
            mongoCredentialList = Collections.emptyList();
        }
    }

    private static Object getProperty(final Map<String, Object> properties, final String name, final String alternativeName) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        } else {
            return properties.get(alternativeName);
        }
    }

    private void parseUrl(final String url) {
        // the url has the following structure:
        // mongodb:[{server}][/{dbName}] [,{server2}[,server3}]]
        final List<HostAndPort> hostAndPortList = new ArrayList<>();

        final String serversAndDbName = url.substring(8); // skip mongodb: part
        final String[] servers = serversAndDbName.split(",");
        for (int i = 0; i < servers.length; i++) {
            String server;
            if (i == 0) {
                final String[] parts = servers[i].split("/");
                if (parts.length == 2) {
                    databaseName = parts[1].trim();
                }
                server = parts[0].trim();
            } else {
                server = servers[i].trim();
            }
            hostAndPortList.add(HostAndPort.fromString(server));
        }

        serverAddresses = hostAndPortList.stream()
                .map(h -> new ServerAddress(h.getHost(), h.hasPort() ? h.getPort() : ServerAddress.defaultPort()))
                .collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
    }
}
