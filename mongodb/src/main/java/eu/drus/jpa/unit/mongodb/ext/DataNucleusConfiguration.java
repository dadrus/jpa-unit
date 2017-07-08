package eu.drus.jpa.unit.mongodb.ext;

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

        parseUrl((String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL));

        final String userName = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_USER, DATANUCLEUS_CONNECTION_USER_NAME);
        final String password = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_PASSWORD, DATANUCLEUS_CONNECTION_PASSWORD);

        if (userName != null) {
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, "admin", password.toCharArray()));
        } else {
            mongoCredentialList = Collections.emptyList();
        }

        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        setOptions(builder, (final String key) -> (String) properties.get(DATANUCLEUS_MONGODB_OPTIONS_PREFIX + "." + key));
        mongoClientOptions = builder.build();

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
        for (final String server : servers) {
            final String[] parts = server.split("/");
            if (parts.length == 2) {
                databaseName = parts[1].trim();
            }
            hostAndPortList.add(HostAndPort.fromString(parts[0].trim()));
        }

        serverAddresses = hostAndPortList.stream()
                .map(h -> new ServerAddress(h.getHost(), h.hasPort() ? h.getPort() : ServerAddress.defaultPort()))
                .collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
    }
}
