package eu.drus.jpa.unit.neo4j.ext;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationRegistryTest {

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private ConfigurationRegistry registry;

    @Before
    public void setUp() {
        registry = new ConfigurationRegistry();
    }

    @Test
    public void testHibernateOgmNeo4jDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_bolt");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertTrue(hasConfiguration);
    }

    @Test
    public void testNonHibernateOgmNeo4jDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "foo");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertFalse(hasConfiguration);
    }

    @Test
    public void testHibernateOgmNeo4jConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "neo4j_http");
        properties.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_HOST, "www.example.com");

        // WHEN
        final Configuration configuration = registry.getConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
    }

    @Test(expected = JpaUnitException.class)
    public void testNonHibernateOgmNeo4jConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put(HibernateOgmConfiguration.HIBERNATE_OGM_DATASTORE_PROVIDER, "foo");

        // WHEN
        registry.getConfiguration(descriptor);

        // THEN
        // exception is thrown
    }
}
