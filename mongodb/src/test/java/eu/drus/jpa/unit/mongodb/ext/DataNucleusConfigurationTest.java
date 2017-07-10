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

import eu.drus.jpa.unit.mongodb.ext.DataNucleusConfiguration.ConfigurationFactoryImpl;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class DataNucleusConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Test
    public void testHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        // mongodb:[{server}][/{dbName}] [,{server2}[,server3}]]

        properties.put("datanucleus.ConnectionURL",
                "mongodb:www.example.com/foo, www2.example.com:123,192.0.2.1, 192.0.2.2:123, 2001:db8::ff00:42:8329, [2001:db8::ff00:42:8329]:123 ");

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final List<ServerAddress> serverAddresses = configuration.getServerAddresses();
        assertThat(serverAddresses, notNullValue());
        assertThat(serverAddresses.size(), equalTo(6));
        assertThat(serverAddresses,
                hasItems(new ServerAddress("www.example.com"), new ServerAddress("www2.example.com", 123), new ServerAddress("192.0.2.1"),
                        new ServerAddress("192.0.2.2", 123), new ServerAddress("2001:db8::ff00:42:8329"),
                        new ServerAddress("[2001:db8::ff00:42:8329]", 123)));
    }

    @Test
    public void testDefaultHost() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");

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

        properties.put("datanucleus.ConnectionURL", "mongodb:192.0.2.2:123/foo");

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

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");
        properties.put("datanucleus.ConnectionPassword", "foo");

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

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");
        properties.put("datanucleus.ConnectionUserName", "user");
        properties.put("datanucleus.ConnectionPassword", "pass");

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

        // it looks like only the two options below are supported by DataNucleus

        // final String description = "Some Description";
        // final String applicationName = "Some Application";
        // final int minConnectionsPerHost = 1;
        final int connectionsPerHost = 2;
        final int threadsAllowedToBlockForConnectionMultiplier = 2;
        // final int serverSelectionTimeout = 500;
        // final int maxWaitTime = 1000;
        // final int maxConnectionIdleTime = 30000;
        // final int maxConnectionLifeTime = 60000;
        // final int connectTimeout = 1000;
        // final int socketTimeout = 1500;
        // final boolean socketKeepAlive = true;
        // final boolean sslEnabled = true;
        // final boolean sslInvalidHostNameAllowed = false;
        // final boolean cursorFinalizerEnabled = false;
        // final boolean alwaysUseMBeans = true;
        // final int heartbeatFrequency = 1600;
        // final int minHeartbeatFrequency = 1700;
        // final int heartbeatConnectTimeout = 1800;
        // final int heartbeatSocketTimeout = 1900;
        // final int localThreshold = 2000;
        // final String requiredReplicaSetName = "Replica Name";

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");

        // properties.put("datanucleus.mongodb.alwaysUseMBeans", "" + alwaysUseMBeans);
        // properties.put("datanucleus.mongodb.applicationName", applicationName);
        properties.put("datanucleus.mongodb.connectionsPerHost", "" + connectionsPerHost);
        // properties.put("datanucleus.mongodb.connectTimeout", "" + connectTimeout);
        // properties.put("datanucleus.mongodb.cursorFinalizerEnabled", "" +
        // cursorFinalizerEnabled);
        // properties.put("datanucleus.mongodb.description", description);
        // properties.put("datanucleus.mongodb.heartbeatConnectTimeout", "" +
        // heartbeatConnectTimeout);
        // properties.put("datanucleus.mongodb.heartbeatFrequency", "" + heartbeatFrequency);
        // properties.put("datanucleus.mongodb.heartbeatSocketTimeout", "" +
        // heartbeatSocketTimeout);
        // properties.put("datanucleus.mongodb.localThreshold", "" + localThreshold);
        // properties.put("datanucleus.mongodb.maxWaitTime", "" + maxWaitTime);
        // properties.put("datanucleus.mongodb.maxConnectionIdleTime", "" + maxConnectionIdleTime);
        // properties.put("datanucleus.mongodb.maxConnectionLifeTime", "" + maxConnectionLifeTime);
        // properties.put("datanucleus.mongodb.minConnectionsPerHost", "" + minConnectionsPerHost);
        // properties.put("datanucleus.mongodb.minHeartbeatFrequency", "" + minHeartbeatFrequency);
        // properties.put("datanucleus.mongodb.requiredReplicaSetName", requiredReplicaSetName);
        // properties.put("datanucleus.mongodb.serverSelectionTimeout", "" +
        // serverSelectionTimeout);
        // properties.put("datanucleus.mongodb.socketKeepAlive", "" + socketKeepAlive);
        // properties.put("datanucleus.mongodb.socketTimeout", "" + socketTimeout);
        // properties.put("datanucleus.mongodb.sslEnabled", "" + sslEnabled);
        // properties.put("datanucleus.mongodb.sslInvalidHostNameAllowed", "" +
        // sslInvalidHostNameAllowed);
        properties.put("datanucleus.mongodb.threadsAllowedToBlockForConnectionMultiplier",
                "" + threadsAllowedToBlockForConnectionMultiplier);

        final ConfigurationFactory factory = new ConfigurationFactoryImpl();

        // WHEN
        final Configuration configuration = factory.createConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());

        final MongoClientOptions clientOptions = configuration.getClientOptions();
        assertThat(clientOptions, notNullValue());
        // assertThat(clientOptions.getApplicationName(), equalTo(applicationName));
        assertThat(clientOptions.getConnectionsPerHost(), equalTo(connectionsPerHost));
        // assertThat(clientOptions.getConnectTimeout(), equalTo(connectTimeout));
        // assertThat(clientOptions.getDescription(), equalTo(description));
        // assertThat(clientOptions.getHeartbeatConnectTimeout(), equalTo(heartbeatConnectTimeout));
        // assertThat(clientOptions.getHeartbeatFrequency(), equalTo(heartbeatFrequency));
        // assertThat(clientOptions.getHeartbeatSocketTimeout(), equalTo(heartbeatSocketTimeout));
        // assertThat(clientOptions.getLocalThreshold(), equalTo(localThreshold));
        // assertThat(clientOptions.getMaxConnectionIdleTime(), equalTo(maxConnectionIdleTime));
        // assertThat(clientOptions.getMaxConnectionLifeTime(), equalTo(maxConnectionLifeTime));
        // assertThat(clientOptions.getMaxWaitTime(), equalTo(maxWaitTime));
        // assertThat(clientOptions.getMinConnectionsPerHost(), equalTo(minConnectionsPerHost));
        // assertThat(clientOptions.getMinHeartbeatFrequency(), equalTo(minHeartbeatFrequency));
        // assertThat(clientOptions.getRequiredReplicaSetName(), equalTo(requiredReplicaSetName));
        // assertThat(clientOptions.getServerSelectionTimeout(), equalTo(serverSelectionTimeout));
        // assertThat(clientOptions.getSocketTimeout(), equalTo(socketTimeout));
        assertThat(clientOptions.getThreadsAllowedToBlockForConnectionMultiplier(), equalTo(threadsAllowedToBlockForConnectionMultiplier));
        // assertThat(clientOptions.isAlwaysUseMBeans(), equalTo(alwaysUseMBeans));
        // assertThat(clientOptions.isCursorFinalizerEnabled(), equalTo(cursorFinalizerEnabled));
        // assertThat(clientOptions.isSocketKeepAlive(), equalTo(socketKeepAlive));
        // assertThat(clientOptions.isSslEnabled(), equalTo(sslEnabled));
        // assertThat(clientOptions.isSslInvalidHostNameAllowed(),
        // equalTo(sslInvalidHostNameAllowed));
    }
}
