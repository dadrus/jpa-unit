package eu.drus.jpa.unit.neo4j.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HibernateOgmConfiguration.class)
public class HibernateOgmConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Mock
    private HikariDataSource dataSource;

    @Before
    public void prepareJpaUnitContext() throws Exception {
        whenNew(HikariDataSource.class).withAnyArguments().thenReturn(dataSource);
    }

    @Test
    public void testNeo4JBoltProtocolSupport() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_bolt");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertTrue(isSupported);
    }

    @Test
    public void testNeo4JHttpProtocolSupport() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_http");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertTrue(isSupported);
    }

    @Test
    public void testNeo4JEmbeddedProtocolSupport() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_embedded");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertTrue(isSupported);
    }

    @Test
    public void testNeo4JUnsupportedProtocol() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_foo");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testNeo4JUnsupportedConfiguration() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("foo", "neo4j_http");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testCreateHttpDataSource() throws SQLException {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_http");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_HOST, "localhost:7474");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_USERNAME, "neo4j");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PASSWORD, "test");

        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();
        final Configuration configuration = factory.createConfiguration(descriptor);

        // WHEN
        final DataSource ds = configuration.createDataSource();

        // THEN
        assertNotNull(ds);

        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> passCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataSource).setDriverClassName(eq("org.neo4j.jdbc.Driver"));
        verify(dataSource).setUsername(userCaptor.capture());
        verify(dataSource).setPassword(passCaptor.capture());
        verify(dataSource).setJdbcUrl(urlCaptor.capture());

        assertThat(urlCaptor.getValue(), equalTo("jdbc:neo4j:http://localhost:7474"));
        assertThat(userCaptor.getValue(), equalTo("neo4j"));
        assertThat(passCaptor.getValue(), equalTo("test"));
    }

    @Test
    public void testCreateBoltDataSource() throws SQLException {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_bolt");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_HOST, "localhost:7687");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_USERNAME, "neo4j");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PASSWORD, "test");

        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();
        final Configuration configuration = factory.createConfiguration(descriptor);

        // WHEN
        final DataSource ds = configuration.createDataSource();

        // THEN
        assertNotNull(ds);

        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> passCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataSource).setDriverClassName(eq("org.neo4j.jdbc.Driver"));
        verify(dataSource).setUsername(userCaptor.capture());
        verify(dataSource).setPassword(passCaptor.capture());
        verify(dataSource).setJdbcUrl(urlCaptor.capture());

        assertThat(urlCaptor.getValue(), equalTo("jdbc:neo4j:bolt://localhost:7687"));
        assertThat(userCaptor.getValue(), equalTo("neo4j"));
        assertThat(passCaptor.getValue(), equalTo("test"));
    }

    @Test
    public void testCreateEmbeddedDataSource() throws SQLException, IOException {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_embedded");
        dbConfig.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATABASE_PATH, System.getProperty("java.io.tmpdir") + "/neo4j_test_db");
        // jpa-unit.properties are used here

        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new HibernateOgmConfiguration.ConfigurationFactoryImpl();
        final Configuration configuration = factory.createConfiguration(descriptor);

        // WHEN
        final DataSource ds = configuration.createDataSource();

        // THEN
        assertNotNull(ds);

        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> passCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataSource).setDriverClassName(eq("org.neo4j.jdbc.Driver"));
        verify(dataSource).setUsername(userCaptor.capture());
        verify(dataSource).setPassword(passCaptor.capture());
        verify(dataSource).setJdbcUrl(urlCaptor.capture());

        assertThat(urlCaptor.getValue(), equalTo("jdbc:neo4j:bolt://localhost:7687"));
        assertThat(userCaptor.getValue(), equalTo("neo4j"));
        assertThat(passCaptor.getValue(), equalTo("test"));
    }

}
