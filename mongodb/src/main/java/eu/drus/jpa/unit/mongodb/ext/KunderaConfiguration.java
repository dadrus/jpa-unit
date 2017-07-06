package eu.drus.jpa.unit.mongodb.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

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
            return dialect != null && dialect.contains("mongodb");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new KunderaConfiguration(descriptor);
        }
    }

    private List<ServerAddress> serverAddresses;
    private String databaseName;
    private List<MongoCredential> mongoCredentialList;
    private MongoClientOptions mongoClientOptions;

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
                    .singletonList(MongoCredential.createPlainCredential(userName, databaseName, toCharArray(password)));
        } else {
            mongoCredentialList = Collections.emptyList();
        }

        // TODO: build MongoClientOptions properly
        mongoClientOptions = MongoClientOptions.builder().build();
    }

    private char[] toCharArray(final String value) {
        return value == null ? null : value.toCharArray();
    }

    @Override
    public List<ServerAddress> getServerAddresses() {
        return serverAddresses;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClientOptions getClientOptions() {
        return mongoClientOptions;
    }

    @Override
    public List<MongoCredential> getCredentials() {
        return mongoCredentialList;
    }

}
