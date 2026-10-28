package eu.drus.jpa.unit.sql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.core.PersistenceUnitDescriptorImpl;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceDecoratorTest {

    private static final Map<String, Object> PROPS = createProperties();

    @Mock
    private ExecutionContext ctx;

    @Mock
    private PersistenceUnitDescriptorImpl descriptor;

    @Mock
    private BasicDataSource ds;

    @Mock
    private TestInvocation invocation;

    private static Map<String, Object> createProperties() {
        final Map<String, Object> dbConfig = new HashMap<>();

        dbConfig.put("jakarta.persistence.jdbc.driver", "driver");
        dbConfig.put("jakarta.persistence.jdbc.url", "url");
        dbConfig.put("jakarta.persistence.jdbc.user", "user");
        dbConfig.put("jakarta.persistence.jdbc.password", "password");

        return dbConfig;
    }

    @Before
    public void prepareMocks() {
        when(ctx.getData(Constants.KEY_DATA_SOURCE)).thenReturn(ds);
        when(ctx.getDescriptor()).thenReturn(descriptor);
        lenient().when(descriptor.getUnitName()).thenReturn("foo");
        when(descriptor.getProperties()).thenReturn(PROPS);

        when(invocation.getContext()).thenReturn(ctx);
        lenient().when(invocation.getTestClass()).thenReturn((Class) getClass());
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

    @Test
    public void testCreateDataSource() throws Throwable {
        // GIVEN
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        final ArgumentCaptor<BasicDataSource> dsCaptor = ArgumentCaptor.forClass(BasicDataSource.class);
        verify(ctx).storeData(eq(Constants.KEY_DATA_SOURCE), dsCaptor.capture());

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
        decorator.afterAll(invocation);

        // THEN
        verify(ds).close();
        verify(ctx).storeData(eq(Constants.KEY_DATA_SOURCE), eq(null));
    }

    @Test
    public void testSupportedConfiguration() {
        // GIVEN
        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        final boolean isSupported = decorator.isConfigurationSupported(ctx);

        // THEN
        assertTrue(isSupported);
    }

    @Test
    public void testUnsupportedConfiguration() {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "driver");
        when(descriptor.getProperties()).thenReturn(props);

        final DataSourceDecorator decorator = new DataSourceDecorator();

        // WHEN
        final boolean isSupported = decorator.isConfigurationSupported(ctx);

        // THEN
        assertFalse(isSupported);
    }
}
