package eu.drus.jpa.unit.mongodb.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.net.HostAndPort;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class KunderaConfiguration extends AbstractConfiguration {

    private static final String KUNDERA_PASSWORD = "kundera.password";
    private static final String KUNDERA_USERNAME = "kundera.username";
    private static final String KUNDERA_KEYSPACE = "kundera.keyspace";
    private static final String KUNDERA_NODES = "kundera.nodes";
    private static final String KUNDERA_PORT = "kundera.port";
    private static final String KUNDERA_DIALECT = "kundera.dialect";

    private static final Map<String, String> KUDERA_KEYS = createKeyMaping();

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> properties = descriptor.getProperties();

            final String dialect = (String) properties.get(KUNDERA_DIALECT);
            return dialect != null && dialect.contains("mongodb");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new KunderaConfiguration(descriptor);
        }
    }

    private KunderaConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddresses(properties);
        configureDatabaseName(properties);
        configureCredentials(properties);
        configureClientOptions(properties);
    }

    private void configureClientOptions(final Map<String, Object> properties) {
        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        setOptions(builder, (final String key) -> (String) properties.get(KUDERA_KEYS.get(key)));
        mongoClientOptions = builder.build();
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) properties.get(KUNDERA_USERNAME);
        final String password = (String) properties.get(KUNDERA_PASSWORD);
        if (userName != null) {
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, "admin", password.toCharArray()));
        } else {
            mongoCredentialList = Collections.emptyList();
        }
    }

    private void configureDatabaseName(final Map<String, Object> properties) {
        databaseName = (String) properties.get(KUNDERA_KEYSPACE);
    }

    private void configureServerAddresses(final Map<String, Object> properties) {
        final String port = (String) properties.get(KUNDERA_PORT);
        final String hosts = (String) properties.get(KUNDERA_NODES);

        final String[] hostList = hosts != null ? hosts.split(",") : new String[] {};
        final Integer defaultPort = port != null ? Integer.valueOf(port) : 27017;

        serverAddresses = new ArrayList<>();
        for (int i = 0; i < hostList.length; i++) {
            final HostAndPort hostAndPort = HostAndPort.fromString(hostList[i].trim());
            serverAddresses.add(new ServerAddress(hostAndPort.getHost(), hostAndPort.getPortOrDefault(defaultPort)));
        }
        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
    }

    private static Map<String, String> createKeyMaping() {
        final Map<String, String> map = new HashMap<>();
        map.put("connectionsPerHost", "connection.perhost");
        // Kundera defines both "connection.perhost" as well as "kundera.pool.size.max.active",
        // which is used by Kundera for the same purpose. Which one should we use?
        map.put("connectTimeout", "connection.timeout");
        map.put("maxWaitTime", "maxwait.time");
        map.put("threadsAllowedToBlockForConnectionMultiplier", "threadsallowed.block.connectionmultiplier");
        map.put("requiredReplicaSetName", "replica.set.name");
        return map;
    }
}
