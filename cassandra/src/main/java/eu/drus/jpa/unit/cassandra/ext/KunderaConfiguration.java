package eu.drus.jpa.unit.cassandra.ext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.google.common.net.HostAndPort;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class KunderaConfiguration implements Configuration {

    private static final String KUNDERA_PASSWORD = "kundera.password";
    private static final String KUNDERA_USERNAME = "kundera.username";
    private static final String KUNDERA_KEYSPACE = "kundera.keyspace";
    private static final String KUNDERA_NODES = "kundera.nodes";
    private static final String KUNDERA_PORT = "kundera.port";
    private static final String KUNDERA_DIALECT = "kundera.dialect";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> properties = descriptor.getProperties();

            final String dialect = (String) properties.get(KUNDERA_DIALECT);
            return dialect != null && dialect.contains("cassandra");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new KunderaConfiguration(descriptor);
        }
    }

    private String keySpace;
    private List<InetSocketAddress> serverAddresses;
    private PlainTextAuthProvider authProvider;

    public KunderaConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddresses(properties);
        configureDatabaseName(properties);
        configureCredentials(properties);
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) properties.get(KUNDERA_USERNAME);
        final String password = (String) properties.get(KUNDERA_PASSWORD);

        if (userName != null && password != null) {
            authProvider = new PlainTextAuthProvider(userName, password);
        }
    }

    private void configureDatabaseName(final Map<String, Object> properties) {
        keySpace = (String) properties.get(KUNDERA_KEYSPACE);
    }

    private void configureServerAddresses(final Map<String, Object> properties) {
        final String port = (String) properties.get(KUNDERA_PORT);
        final String hosts = (String) properties.get(KUNDERA_NODES);

        final String[] hostList = hosts != null ? hosts.split(",") : new String[] {};
        final Integer defaultPort = port != null ? Integer.valueOf(port) : 9042;

        serverAddresses = new ArrayList<>();
        for (int i = 0; i < hostList.length; i++) {
            final HostAndPort hostAndPort = HostAndPort.fromString(hostList[i].trim());
            serverAddresses.add(new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPortOrDefault(defaultPort)));
        }
        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new InetSocketAddress("127.0.0.1", defaultPort));
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
