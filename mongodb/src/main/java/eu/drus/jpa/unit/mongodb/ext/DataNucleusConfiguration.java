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

public class DataNucleusConfiguration implements Configuration {

    private static final String DATANUCLEUS_CONNECTION_URL = "datanucleus.ConnectionURL";
    private static final String DATANUCLEUS_CONNECTION_PASSWORD = "datanucleus.ConnectionPassword";
    private static final String DATANUCLEUS_CONNECTION_USER_NAME = "datanucleus.ConnectionUserName";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";
    private static final String MONGODB_CONNECTIONS_PER_HOST = "datanucleus.mongodb.connectionsPerHost";
    private static final String MONGODB_THREAD_BLOCK_FOR_MULTIPLIER = "datanucleus.mongodb.threadsAllowedToBlockForConnectionMultiplier";

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

    private List<ServerAddress> serverAddresses;
    private String databaseName;
    private List<MongoCredential> mongoCredentialList;
    private MongoClientOptions mongoClientOptions;

    private DataNucleusConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        parseUrl((String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_URL, DATANUCLEUS_CONNECTION_URL));

        final String userName = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_USER, DATANUCLEUS_CONNECTION_USER_NAME);
        final String password = (String) getProperty(properties, JAVAX_PERSISTENCE_JDBC_PASSWORD, DATANUCLEUS_CONNECTION_PASSWORD);

        if (userName != null) {
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, databaseName, toCharArray(password)));
        } else {
            mongoCredentialList = Collections.emptyList();
        }

        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        if (properties.containsKey(MONGODB_CONNECTIONS_PER_HOST)) {
            builder.connectionsPerHost(Integer.valueOf((String) properties.get(MONGODB_CONNECTIONS_PER_HOST)));
        }
        if (properties.containsKey(MONGODB_THREAD_BLOCK_FOR_MULTIPLIER)) {
            builder.threadsAllowedToBlockForConnectionMultiplier(
                    Integer.valueOf((String) properties.get(MONGODB_THREAD_BLOCK_FOR_MULTIPLIER)));
        }
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
        // the url has the following structure: mongodb:[{server}][/{dbName}]
        // [,{server2}[,server3}]]
        final List<HostAndPort> hostAndPortList = new ArrayList<>();

        final String serversAndDbName = url.substring(8); // skip mongodb: part
        String[] parts = serversAndDbName.split("/");
        if (parts[0].isEmpty()) {
            // only db name is given
            databaseName = parts[1].trim();
        } else {
            // at least one server is given
            hostAndPortList.add(HostAndPort.fromString(parts[0].trim()));

            // check whether there are further server defined
            parts = parts[1].split(",");
            databaseName = parts[0].trim();
            for (int i = 1; i < parts.length; i++) {
                hostAndPortList.add(HostAndPort.fromString(parts[i].trim()));
            }
        }

        serverAddresses = hostAndPortList.stream().map(h -> new ServerAddress(h.getHost(), h.getPort())).collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
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
