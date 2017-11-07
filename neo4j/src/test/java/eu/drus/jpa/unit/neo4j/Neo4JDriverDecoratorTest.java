package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.neo4j.ext.Configuration;
import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Neo4JDriverDecorator.class)
public class Neo4JDriverDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private TestInvocation invocation;

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Configuration configuration;

    @Mock
    private ConfigurationRegistry configRegistry;

    private Neo4JDriverDecorator decorator;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Before
    public void prepareTest() throws Exception {
        whenNew(ConfigurationRegistry.class).withAnyArguments().thenReturn(configRegistry);
        whenNew(HikariDataSource.class).withAnyArguments().thenReturn(dataSource);

        when(configuration.createDataSource()).thenReturn(dataSource);
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTestClass()).thenReturn((Class) getClass());
        when(ctx.getData(eq(Constants.KEY_DATA_SOURCE))).thenReturn(dataSource);
        when(configRegistry.getConfiguration(any(PersistenceUnitDescriptor.class))).thenReturn(configuration);

        decorator = new Neo4JDriverDecorator();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(0));
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_DATA_SOURCE), eq(dataSource));
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.afterAll(invocation);

        // THEN
        verify(dataSource).close();
        verify(ctx).storeData(eq(Constants.KEY_DATA_SOURCE), isNull());
    }
}
