package eu.drus.jpa.unit.neo4j.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
@PrepareForTest(DataNucleusConfiguration.class)
public class DataNucleusConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Mock
    private HikariDataSource dataSource;

    @Before
    public void prepareJpaUnitContext() throws Exception {
        whenNew(HikariDataSource.class).withAnyArguments().thenReturn(dataSource);
    }

    @Test
    public void testNeo4JUnsupportedProtocol() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(DataNucleusConfiguration.DATANUCLEUS_CONNECTION_URL, "neo4k:/some_path");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new DataNucleusConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testNeo4JUnsupportedConfiguration() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("foo", "neo4j:/some_path");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new DataNucleusConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testCreateEmbeddedDataSource() throws SQLException, IOException {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(DataNucleusConfiguration.DATANUCLEUS_CONNECTION_URL,
                "neo4j:" + System.getProperty("java.io.tmpdir") + "/neo4j_test_db");
        // jpa-unit.properties are used here

        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new DataNucleusConfiguration.ConfigurationFactoryImpl();
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
