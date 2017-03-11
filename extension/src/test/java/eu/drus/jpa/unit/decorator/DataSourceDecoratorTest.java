package eu.drus.jpa.unit.decorator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptorLoader;
import eu.drus.jpa.unit.spi.ExecutionContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataSourceDecorator.class)
public class DataSourceDecoratorTest {

    private static final Map<String, Object> PROPS = createProperties();

    @Mock
    private ExecutionContext ctx;

    @Mock
    private PersistenceUnitDescriptorLoader descriptorLoader;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Mock
    private BasicDataSource ds;

    private static Map<String, Object> createProperties() {
        final Map<String, Object> dbConfig = new HashMap<>();

        dbConfig.put("javax.persistence.jdbc.driver", "driver");
        dbConfig.put("javax.persistence.jdbc.url", "url");
        dbConfig.put("javax.persistence.jdbc.user", "user");
        dbConfig.put("javax.persistence.jdbc.password", "password");

        return dbConfig;
    }

    @Before
    @SuppressWarnings("unchecked")
    public void prepareMocks() throws Exception {
        whenNew(PersistenceUnitDescriptorLoader.class).withAnyArguments().thenReturn(descriptorLoader);

        when(ctx.getData("properties")).thenReturn(PROPS);
        when(ctx.getData("unitName")).thenReturn("foo");
        when(descriptor.getUnitName()).thenReturn("foo");
        when(descriptor.getProperties()).thenReturn(PROPS);
        when(ctx.getData("ds")).thenReturn(ds);
        when(descriptorLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenReturn(Arrays.asList(descriptor));
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(0));
    }

    @Test(expected = JpaUnitException.class)
    public void testCantCreateDataSourceDueToUnknownPersistenceUnitName() throws Throwable {
        // GIVEN
        when(descriptor.getUnitName()).thenReturn("someName");
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        decorator.beforeAll(ctx, this);

        // THEN
        // JpaUnitException due to unknown unitName
    }

    @Test(expected = JpaUnitException.class)
    @SuppressWarnings("unchecked")
    public void testCantCreateDataSourceDueToPersistenceUnitNameAmbiguity() throws Throwable {
        // GIVEN
        when(descriptorLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenReturn(Arrays.asList(descriptor, descriptor));
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        decorator.beforeAll(ctx, this);

        // THEN
        // JpaUnitException due to ambiguity
    }

    @Test
    public void testCreateDataSource() throws Throwable {
        // GIVEN
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        decorator.beforeAll(ctx, this);

        // THEN
        final ArgumentCaptor<BasicDataSource> dsCaptor = ArgumentCaptor.forClass(BasicDataSource.class);
        verify(ctx).storeData(eq("ds"), dsCaptor.capture());

        final BasicDataSource dataSource = dsCaptor.getValue();

        assertThat(dataSource.getUsername(), equalTo("user"));
        assertThat(dataSource.getPassword(), equalTo("password"));
        assertThat(dataSource.getDriverClassName(), equalTo("driver"));
        assertThat(dataSource.getUrl(), equalTo("url"));
    }

    @Test
    public void testCloseDataSource() throws Throwable {
        // GIVEN
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        decorator.afterAll(ctx, this);

        // THEN
        verify(ds).close();
        verify(ctx).storeData(eq("ds"), eq(null));
    }
}
