package eu.drus.jpa.unit.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.net.HostAndPort;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class MongoDbConfiguration {

    private Map<String, Object> dbConfig;
    private String providerClassName;

    public MongoDbConfiguration(final PersistenceUnitDescriptor descriptor) {
        dbConfig = descriptor.getProperties();
        providerClassName = descriptor.getProviderClassName();
    }

    public static boolean isSupported(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return dbConfig.containsKey("hibernate.ogm.datastore.database");
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

    public List<ServerAddress> getServerAddresses() {
        final List<HostAndPort> hostsAndPorts = parse((String) dbConfig.get("hibernate.ogm.datastore.host"));
        final List<ServerAddress> serverAddresses = hostsAndPorts.stream().map(h -> new ServerAddress(h.getHost(), h.getPort()))
                .collect(Collectors.toList());

        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
        return serverAddresses;
    }

    public String getDatabaseName() {
        return (String) dbConfig.get("hibernate.ogm.datastore.database");
    }

    public MongoClientOptions getClientOptions() {
        // TODO: implement support for options
        return MongoClientOptions.builder().build();
    }

    public MongoClient createMongoClient() {
        if (providerClassName.equals("org.hibernate.ogm.datastore.mongodb.impl.FongoDBDatastoreProvider")) {
            // inmemory db is used.

            throw new JpaUnitException("In-memory data base is not supported");
        }

        return new MongoClient(getServerAddresses(), getCredentials(), getClientOptions());
    }

    public List<MongoCredential> getCredentials() {
        final String userName = (String) dbConfig.get("hibernate.ogm.datastore.username");
        final String password = (String) dbConfig.get("hibernate.ogm.datastore.password");
        final String databaseName = (String) dbConfig.get("hibernate.ogm.mongodb.authentication_database");
        if (userName != null) {
            return Collections.singletonList(MongoCredential.createPlainCredential(userName, databaseName, toCharArray(password)));
        }
        return Collections.emptyList();
    }
}
