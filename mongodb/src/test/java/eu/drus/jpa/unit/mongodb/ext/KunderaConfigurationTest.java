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
import com.mongodb.ServerAddress;

import eu.drus.jpa.unit.mongodb.ext.KunderaConfiguration.ConfigurationFactoryImpl;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class KunderaConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Test
    public void testHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("kundera.keyspace", "foo");
        properties.put("kundera.nodes", "www.example.com, 192.0.2.2:123, [2001:db8::ff00:42:8329]:27027, www2.example.com");
        properties.put("kundera.port", "27016");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<ServerAddress> serverAddresses = configuration.getServerAddresses();
        assertThat(serverAddresses, notNullValue());
        assertThat(serverAddresses.size(), equalTo(4));
        assertThat(serverAddresses, hasItems(new ServerAddress("www.example.com", 27016), new ServerAddress("192.0.2.2", 123),
                new ServerAddress("2001:db8::ff00:42:8329", 27027), new ServerAddress("www2.example.com", 27016)));
    }

    @Test
    public void testDefaultHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("kundera.keyspace", "foo");

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

        properties.put("kundera.keyspace", "foo");

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

        properties.put("kundera.keyspace", "foo");
        properties.put("kundera.password", "pass");

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

        properties.put("kundera.keyspace", "foo");
        properties.put("kundera.username", "user");
        properties.put("kundera.password", "pass");

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

        properties.put("kundera.keyspace", "foo");

        final int connectionsPerHost = 1;
        final int connectTimeout = 2;
        final int maxWaitTime = 3;
        final int threadsAllowedToBlockForConnectionMultiplier = 4;
        final String requiredReplicaSetName = "Some Replica Name";

        // it looks like only the two options below are supported by Kundera
        properties.put("connection.perhost", "" + 1);
        properties.put("connection.timeout", "" + connectTimeout);
        properties.put("maxwait.time", "" + maxWaitTime);
        properties.put("threadsallowed.block.connectionmultiplier", "" + threadsAllowedToBlockForConnectionMultiplier);
        properties.put("replica.set.name", requiredReplicaSetName);

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final MongoClientOptions clientOptions = configuration.getClientOptions();
        assertThat(clientOptions, notNullValue());
        assertThat(clientOptions.getConnectionsPerHost(), equalTo(connectionsPerHost));
        assertThat(clientOptions.getConnectTimeout(), equalTo(connectTimeout));
        assertThat(clientOptions.getMaxWaitTime(), equalTo(maxWaitTime));
        assertThat(clientOptions.getThreadsAllowedToBlockForConnectionMultiplier(), equalTo(threadsAllowedToBlockForConnectionMultiplier));
        assertThat(clientOptions.getRequiredReplicaSetName(), equalTo(requiredReplicaSetName));
    }
}
