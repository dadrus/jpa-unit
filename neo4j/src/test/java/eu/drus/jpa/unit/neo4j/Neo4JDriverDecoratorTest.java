package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.neo4j.ext.Configuration;
import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
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
    private PersistenceUnitDescriptor descriptor;

    private ConfigurationRegistry configRegistry;

    private Neo4JDriverDecorator decorator;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Before
    public void prepareTest() throws Exception {
        try (final MockedConstruction<ConfigurationRegistry> constructions = mockConstruction(ConfigurationRegistry.class)) {
            decorator = new Neo4JDriverDecorator();
            configRegistry = constructions.constructed().get(0);
        }

        lenient().when(configRegistry.getConfiguration(any(PersistenceUnitDescriptor.class))).thenReturn(configuration);
        lenient().when(configuration.createDataSource()).thenReturn(dataSource);
        lenient().when(invocation.getContext()).thenReturn(ctx);
        lenient().when(ctx.getDescriptor()).thenReturn(descriptor);
        lenient().when(invocation.getTestClass()).thenReturn((Class) getClass());
        lenient().when(ctx.getData(eq(Constants.KEY_DATA_SOURCE))).thenReturn(dataSource);
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
