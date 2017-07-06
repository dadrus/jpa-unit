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

public class HibernateOgmConfiguration implements Configuration {

    private static final String HIBERNATE_OGM_MONGODB_AUTHENTICATION_DATABASE = "hibernate.ogm.mongodb.authentication_database";
    private static final String HIBERNATE_OGM_DATASTORE_PASSWORD = "hibernate.ogm.datastore.password";
    private static final String HIBERNATE_OGM_DATASTORE_USERNAME = "hibernate.ogm.datastore.username";
    private static final String HIBERNATE_OGM_DATASTORE_HOST = "hibernate.ogm.datastore.host";
    private static final String HIBERNATE_OGM_DATASTORE_DATABASE = "hibernate.ogm.datastore.database";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> dbConfig = descriptor.getProperties();

            return dbConfig.containsKey(HIBERNATE_OGM_DATASTORE_DATABASE);
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new HibernateOgmConfiguration(descriptor);
        }
    }

    private List<ServerAddress> serverAddresses;
    private String databaseName;
    private List<MongoCredential> mongoCredentialList;
    private MongoClientOptions mongoClientOptions;

    private HibernateOgmConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        final List<HostAndPort> hostsAndPorts = parse((String) properties.get(HIBERNATE_OGM_DATASTORE_HOST));
        serverAddresses = hostsAndPorts.stream().map(h -> new ServerAddress(h.getHost(), h.getPort())).collect(Collectors.toList());
        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }

        databaseName = (String) properties.get(HIBERNATE_OGM_DATASTORE_DATABASE);

        final String userName = (String) properties.get(HIBERNATE_OGM_DATASTORE_USERNAME);
        final String password = (String) properties.get(HIBERNATE_OGM_DATASTORE_PASSWORD);
        final String authDatabaseName = (String) properties.get(HIBERNATE_OGM_MONGODB_AUTHENTICATION_DATABASE);
        if (userName != null) {
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, authDatabaseName, toCharArray(password)));
        } else {
            mongoCredentialList = Collections.emptyList();
        }

        // TODO: build MongoClientOptions properly
        mongoClientOptions = MongoClientOptions.builder().build();
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
