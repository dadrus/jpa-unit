package eu.drus.jpa.unit.cassandra.ext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.google.common.net.HostAndPort;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class DataNucleusConfiguration implements Configuration {

    private static final String DATANUCLEUS_MONGODB_OPTIONS_PREFIX = "datanucleus.mongodb";
    private static final String DATANUCLEUS_CONNECTION_URL = "datanucleus.ConnectionURL";
    private static final String DATANUCLEUS_CONNECTION_PASSWORD = "datanucleus.ConnectionPassword";
    private static final String DATANUCLEUS_CONNECTION_USER_NAME = "datanucleus.ConnectionUserName";
    private static final String DATANUCLEUS_MAPPING_SCHEMA = "datanucleus.mapping.Schema";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final String url = (String) getProperty(descriptor.getProperties(), JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL);

            return url != null ? url.startsWith("cassandra:") : false;
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new DataNucleusConfiguration(descriptor);
        }
    }

    private AuthProvider authProvider;
    private List<InetSocketAddress> serverAddresses;
    private String databaseName;
    private String keySpace;

    private DataNucleusConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddressesAndDatabaseName(properties);
        configureCredentials(properties);
        configureSchema(properties);
    }

    private void configureSchema(final Map<String, Object> properties) {
        keySpace = (String) properties.get(DATANUCLEUS_MAPPING_SCHEMA);

    }

    private void configureServerAddressesAndDatabaseName(final Map<String, Object> properties) {
        parseUrl((String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL));
        // checkArgument(databaseName != null, DATANUCLEUS_CONNECTION_URL + " does not contain a
        // database name part");
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_USER, DATANUCLEUS_CONNECTION_USER_NAME);
        final String password = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_PASSWORD, DATANUCLEUS_CONNECTION_PASSWORD);

        if (userName != null && password != null) {
            authProvider = new PlainTextAuthProvider(userName, password);
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
        // cassandra:[{server}][/{dbName}] [,{server2}[,server3}]]
        final List<HostAndPort> hostAndPortList = new ArrayList<>();

        final String serversAndDbName = url.substring(10); // skip cassandra: part
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

        serverAddresses = hostAndPortList.stream().map(h -> new InetSocketAddress(h.getHost(), h.hasPort() ? h.getPort() : 9042))
                .collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new InetSocketAddress("127.0.0.1", 9042));
        }
    }

    @Override
    public Cluster openCluster() {
        return Cluster.builder().addContactPointsWithPorts(serverAddresses).withAuthProvider(authProvider).build();
    }

    @Override
    public String getKeySpace() {
        return keySpace;
    }
}
