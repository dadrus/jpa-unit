package eu.drus.jpa.unit.mongodb.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import eu.drus.jpa.unit.mongodb.ext.EclipseLinkConfiguration.ConfigurationFactoryImpl;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class EclipseLinkConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Test
    public void testHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");
        properties.put("eclipselink.nosql.property.mongo.host", "www.example.com, 192.0.2.2, 2001:db8::ff00:42:8329, www2.example.com");
        properties.put("eclipselink.nosql.property.mongo.port", "27016, 27001, 123");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<ServerAddress> serverAddresses = configuration.getServerAddresses();
        assertThat(serverAddresses, notNullValue());
        assertThat(serverAddresses.size(), equalTo(4));
        assertThat(serverAddresses, hasItems(new ServerAddress("www.example.com", 27016), new ServerAddress("192.0.2.2", 27001),
                new ServerAddress("2001:db8::ff00:42:8329", 123), new ServerAddress("www2.example.com")));
    }

    @Test
    public void testDefaultHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<ServerAddress> serverAddresses = configuration.getServerAddresses();
        assertThat(serverAddresses, notNullValue());
        assertThat(serverAddresses.size(), equalTo(1));
        assertThat(serverAddresses, hasItems(new ServerAddress("127.0.0.1")));
    }

    @Test
    public void testDatabaseName() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
        assertThat(configuration.getDatabaseName(), equalTo("foo"));
    }

    @Test
    public void testMongoCredentialsAreEmptyIfUsernameIsNotConfigured() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");
        properties.put("eclipselink.nosql.property.password", "pass");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<MongoCredential> credentials = configuration.getCredentials();
        assertThat(credentials, notNullValue());
        assertTrue(credentials.isEmpty());
    }

    @Test
    public void testMongoCredentials() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");
        properties.put("eclipselink.nosql.property.user", "user");
        properties.put("eclipselink.nosql.property.password", "pass");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<MongoCredential> credentials = configuration.getCredentials();
        assertThat(credentials, notNullValue());
        assertThat(credentials.size(), equalTo(1));

        final MongoCredential mongoCredential = credentials.get(0);
        assertThat(mongoCredential, notNullValue());
        assertThat(mongoCredential.getUserName(), equalTo("user"));
        assertThat(mongoCredential.getPassword(), equalTo("pass".toCharArray()));
        assertThat(mongoCredential.getSource(), equalTo("admin"));
        assertThat(mongoCredential.getAuthenticationMechanism(), equalTo(AuthenticationMechanism.PLAIN));
    }

    @Test
    public void testMongoClientOptions() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.nosql.property.mongo.db", "foo");

        // it looks like only the two options below are supported by EclipseLink

        final ReadPreference readPreference = ReadPreference.nearest();
        final WriteConcern writeConcern = WriteConcern.JOURNALED;

        properties.put("eclipselink.nosql.property.mongo.read-preference", readPreference.getName());
        properties.put("eclipselink.nosql.property.mongo.write-concern", "JOURNALED");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final MongoClientOptions clientOptions = configuration.getClientOptions();
        assertThat(clientOptions, notNullValue());
        assertThat(clientOptions.getReadPreference(), equalTo(readPreference));
        assertThat(clientOptions.getWriteConcern(), equalTo(writeConcern));
    }
}
