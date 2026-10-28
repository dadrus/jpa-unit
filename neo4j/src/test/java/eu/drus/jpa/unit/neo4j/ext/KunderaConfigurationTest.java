package eu.drus.jpa.unit.neo4j.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class KunderaConfigurationTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private HikariDataSource dataSource;

    @Test
    public void testNeo4JUnsupportedProtocol() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(KunderaConfiguration.KUNDERA_DIALECT, "neo4k");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new KunderaConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testNeo4JUnsupportedConfiguration() {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("foo", "neo4j");
        when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new KunderaConfiguration.ConfigurationFactoryImpl();

        // WHEN
        final boolean isSupported = factory.isSupported(descriptor);

        // THEN
        assertFalse(isSupported);
    }

    @Test
    public void testCreateEmbeddedDataSource() throws SQLException, IOException {
        // GIVEN
        final Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put(KunderaConfiguration.KUNDERA_DIALECT, "neo4j");
        // jpa-unit.properties are used here

        lenient().when(descriptor.getProperties()).thenReturn(dbConfig);
        final ConfigurationFactory factory = new KunderaConfiguration.ConfigurationFactoryImpl();
        final Configuration configuration = factory.createConfiguration(descriptor);

        // WHEN
        final DataSource ds;
        try (final MockedConstruction<HikariDataSource> dataSources = mockConstruction(HikariDataSource.class)) {
            ds = configuration.createDataSource();
            dataSource = dataSources.constructed().get(0);

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
}
