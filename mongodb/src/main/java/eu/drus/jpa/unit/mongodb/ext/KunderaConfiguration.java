package eu.drus.jpa.unit.mongodb.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

        final String port = (String) properties.get(KUNDERA_PORT);
        final String host = (String) properties.get(KUNDERA_NODES);

        serverAddresses = new ArrayList<>();
        serverAddresses.add(new ServerAddress(host.trim(), Integer.valueOf(port.trim())));
        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }

        databaseName = (String) properties.get(KUNDERA_KEYSPACE);

        final String userName = (String) properties.get(KUNDERA_USERNAME);
        final String password = (String) properties.get(KUNDERA_PASSWORD);
        if (userName != null) {
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, "admin", password.toCharArray()));
        } else {
            mongoCredentialList = Collections.emptyList();
        }

        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        setOptions(builder, (final String key) -> (String) properties.get(KUDERA_KEYS.get(key)));
        mongoClientOptions = builder.build();
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
