package eu.drus.jpa.unit.cassandra.ext;

import static com.google.common.base.Preconditions.checkArgument;

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

public class HibernateOgmConfiguration implements Configuration {

    private static final String HIBERNATE_OGM_DATASTORE_PASSWORD = "hibernate.ogm.datastore.password";
    private static final String HIBERNATE_OGM_DATASTORE_USERNAME = "hibernate.ogm.datastore.username";
    private static final String HIBERNATE_OGM_DATASTORE_HOST = "hibernate.ogm.datastore.host";
    private static final String HIBERNATE_OGM_DATASTORE_DATABASE = "hibernate.ogm.datastore.database";
    private static final String HIBERNATE_OGM_DATASTORE_PROVIDER = "hibernate.ogm.datastore.provider";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> dbConfig = descriptor.getProperties();

            return dbConfig.containsKey(HIBERNATE_OGM_DATASTORE_PROVIDER)
                    && ((String) dbConfig.get(HIBERNATE_OGM_DATASTORE_PROVIDER)).contains("cassandra");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new HibernateOgmConfiguration(descriptor);
        }
    }

    private AuthProvider authProvider;
    private String databaseName;
    private List<InetSocketAddress> serverAddresses;

    private HibernateOgmConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddresses(properties);
        configureDatabaseName(properties);
        configureCredentials(properties);
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) properties.get(HIBERNATE_OGM_DATASTORE_USERNAME);
        final String password = (String) properties.get(HIBERNATE_OGM_DATASTORE_PASSWORD);

        if (userName != null && password != null) {
            authProvider = new PlainTextAuthProvider(userName, password);
        }
    }

    private void configureDatabaseName(final Map<String, Object> properties) {
        databaseName = (String) properties.get(HIBERNATE_OGM_DATASTORE_DATABASE);
        checkArgument(databaseName != null, HIBERNATE_OGM_DATASTORE_DATABASE + " was not configured, but required");
    }

    private void configureServerAddresses(final Map<String, Object> properties) {
        final List<HostAndPort> hostsAndPorts = parse((String) properties.get(HIBERNATE_OGM_DATASTORE_HOST));

        serverAddresses = hostsAndPorts.stream().map(h -> new InetSocketAddress(h.getHost(), h.hasPort() ? h.getPort() : 9042))
                .collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new InetSocketAddress("127.0.0.1", 9042));
        }
    }

    private List<HostAndPort> parse(final String hostString) {
        final List<HostAndPort> hostAndPorts = new ArrayList<>();
        if (hostString == null || hostString.trim().isEmpty()) {
            return hostAndPorts;
        }

        for (final String rawSplit : hostString.split(",")) {
            hostAndPorts.add(HostAndPort.fromString(rawSplit.trim()));
        }
        return hostAndPorts;
    }

    @Override
    public Cluster openCluster() {
        return Cluster.builder().addContactPointsWithPorts(serverAddresses).withAuthProvider(authProvider).build();
    }

    @Override
    public String getKeySpace() {
        return databaseName;
    }
}
