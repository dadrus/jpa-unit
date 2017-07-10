package eu.drus.jpa.unit.mongodb.ext;

import static org.hamcrest.CoreMatchers.notNullValue;
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
    public void testHibernateOgmMongoDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("hibernate.ogm.datastore.provider", "org.hibernate.ogm.datastore.mongodb.impl.MongoDBDatastoreProvider");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertTrue(hasConfiguration);
    }

    @Test
    public void testHibernateOgmMongoDbConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("hibernate.ogm.datastore.provider", "org.hibernate.ogm.datastore.mongodb.impl.MongoDBDatastoreProvider");
        properties.put("hibernate.ogm.datastore.database", "foo");
        properties.put("hibernate.ogm.datastore.host", "www.example.com");

        // WHEN
        final Configuration configuration = registry.getConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
    }

    @Test
    public void testEclipseLinkMongoDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.target-database", "org.eclipse.persistence.nosql.adapters.mongo.MongoPlatform");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertTrue(hasConfiguration);
    }

    @Test
    public void testEclipseLinkMongoDbConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("eclipselink.target-database", "org.eclipse.persistence.nosql.adapters.mongo.MongoPlatform");
        properties.put("eclipselink.nosql.property.mongo.db", "foo");
        properties.put("eclipselink.nosql.property.mongo.host", "www.example.com");

        // WHEN
        final Configuration configuration = registry.getConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
    }

    @Test
    public void testDataNucleusMongoDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertTrue(hasConfiguration);
    }

    @Test
    public void testDataNucleusMongoDbConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("datanucleus.ConnectionURL", "mongodb:/foo");

        // WHEN
        final Configuration configuration = registry.getConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
    }

    @Test
    public void testKunderaMongoDbConfigurationIsAvailable() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("kundera.dialect", "mongodb");

        // WHEN
        final boolean hasConfiguration = registry.hasConfiguration(descriptor);

        // THEN
        assertTrue(hasConfiguration);
    }

    @Test
    public void testKunderaMongoDbConfigurationCanBeAccessed() {
        // GIVEN
        final Map<String, Object> properties = new HashMap<>();
        when(descriptor.getProperties()).thenReturn(properties);

        properties.put("kundera.dialect", "mongodb");
        properties.put("kundera.keyspace", "foo");
        properties.put("kundera.nodes", "www.example.com");

        // WHEN
        final Configuration configuration = registry.getConfiguration(descriptor);

        // THEN
        assertThat(configuration, notNullValue());
    }
}
