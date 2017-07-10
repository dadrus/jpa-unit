package eu.drus.jpa.unit.mongodb.ext;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class EclipseLinkConfiguration extends AbstractConfiguration {

    private static final String ECLIPSELINK_NOSQL_PROPERTY_MONGO_READ_PREFERENCE = "eclipselink.nosql.property.mongo.read-preference";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_MONGO_WRITE_CONCERN = "eclipselink.nosql.property.mongo.write-concern";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_PASSWORD = "eclipselink.nosql.property.password";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_USER = "eclipselink.nosql.property.user";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_MONGO_DB = "eclipselink.nosql.property.mongo.db";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_MONGO_HOST = "eclipselink.nosql.property.mongo.host";
    private static final String ECLIPSELINK_NOSQL_PROPERTY_MONGO_PORT = "eclipselink.nosql.property.mongo.port";
    private static final String ECLIPSELINK_TARGET_DATABASE = "eclipselink.target-database";

    public static class ConfigurationFactoryImpl implements ConfigurationFactory {

        @Override
        public boolean isSupported(final PersistenceUnitDescriptor descriptor) {
            final Map<String, Object> properties = descriptor.getProperties();

            final String tgtDataBase = (String) properties.get(ECLIPSELINK_TARGET_DATABASE);
            return tgtDataBase != null && tgtDataBase.contains("mongo");
        }

        @Override
        public Configuration createConfiguration(final PersistenceUnitDescriptor descriptor) {
            return new EclipseLinkConfiguration(descriptor);
        }
    }

    private EclipseLinkConfiguration(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> properties = descriptor.getProperties();

        configureServerAddresses(properties);
        configureDatabaseName(properties);
        configureCredentials(properties);
        configureClientOptions(properties);
    }

    private void configureClientOptions(final Map<String, Object> properties) {
        final MongoClientOptions.Builder builder = MongoClientOptions.builder();
        final String writeConcern = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_MONGO_WRITE_CONCERN);
        final String readPreference = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_MONGO_READ_PREFERENCE);

        if (writeConcern != null) {
            builder.writeConcern(WriteConcern.valueOf(writeConcern));
        }
        if (readPreference != null) {
            builder.readPreference(ReadPreference.valueOf(readPreference));
        }

        mongoClientOptions = builder.build();
    }

    private void configureCredentials(final Map<String, Object> properties) {
        final String userName = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_USER);
        final String password = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_PASSWORD);
        if (userName != null) {
            checkArgument(password != null, ECLIPSELINK_NOSQL_PROPERTY_PASSWORD + " was not configured, but required");
            mongoCredentialList = Collections
                    .singletonList(MongoCredential.createPlainCredential(userName, "admin", password.toCharArray()));
        } else {
            mongoCredentialList = Collections.emptyList();
        }
    }

    private void configureDatabaseName(final Map<String, Object> properties) {
        databaseName = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_MONGO_DB);
        checkArgument(databaseName != null, ECLIPSELINK_NOSQL_PROPERTY_MONGO_DB + " was not configured, but required");
    }

    private void configureServerAddresses(final Map<String, Object> properties) {
        final String ports = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_MONGO_PORT);
        final String hosts = (String) properties.get(ECLIPSELINK_NOSQL_PROPERTY_MONGO_HOST);
        final String[] hostList = hosts != null ? hosts.split(",") : new String[] {};
        final String[] portList = ports != null ? ports.split(",") : new String[] {};

        serverAddresses = new ArrayList<>();
        for (int i = 0; i < hostList.length; i++) {
            int port;
            if (i >= portList.length) {
                port = ServerAddress.defaultPort();
            } else {
                port = Integer.valueOf(portList[i].trim());
            }
            serverAddresses.add(new ServerAddress(hostList[i].trim(), port));
        }
        if (serverAddresses.isEmpty()) {
            serverAddresses.add(new ServerAddress());
        }
    }
}
