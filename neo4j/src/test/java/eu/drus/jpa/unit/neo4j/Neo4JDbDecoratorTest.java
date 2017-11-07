package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.sql.Connection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Neo4JDbDecorator.class)
public class Neo4JDbDecoratorTest {

    @Mock
    private ConfigurationRegistry configRegistry;

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private Neo4JDbFeatureExecutor executor;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private Neo4JDbDecorator decorator;

    @Before
    public void prepareTest() throws Exception {
        whenNew(ConfigurationRegistry.class).withAnyArguments().thenReturn(configRegistry);
        whenNew(Neo4JDbFeatureExecutor.class).withAnyArguments().thenReturn(executor);

        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getException()).thenReturn(Optional.empty());
        when(dataSource.getConnection()).thenReturn(connection);
        when(ctx.getData(eq(Constants.KEY_DATA_SOURCE))).thenReturn(dataSource);
        when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        when(ctx.getDescriptor()).thenReturn(descriptor);

        decorator = new Neo4JDbDecorator();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(5));
    }

    @Test
    public void testBeforeTest() throws Exception {
        // GIVEN

        // WHEN
        decorator.beforeTest(invocation);

        // THEN
        verify(connection).setAutoCommit(eq(Boolean.FALSE));
        verify(executor).executeBeforeTest(eq(connection));
        verify(ctx).storeData(eq(Constants.KEY_CONNECTION), eq(connection));
    }

    @Test
    public void testAfterTestExecutesWithoutErrors() throws Exception {
        // GIVEN

        // WHEN
        decorator.afterTest(invocation);

        // THEN
        verify(connection).close();
        verify(executor).executeAfterTest(eq(connection), eq(Boolean.FALSE));
        verify(ctx).storeData(eq(Constants.KEY_CONNECTION), isNull());
        verifyZeroInteractions(dataSource);
    }

    @Test
    public void testAfterTestExecutesWithErrors() throws Exception {
        // GIVEN
        doThrow(RuntimeException.class).when(executor).executeAfterTest(eq(connection), eq(Boolean.FALSE));

        // WHEN
        try {
            decorator.afterTest(invocation);
            fail("Exception expected");
        } catch (final RuntimeException e) {
            // expected
        }

        // THEN
        verify(connection).close();
        verify(ctx).storeData(eq(Constants.KEY_CONNECTION), isNull());
        verifyZeroInteractions(dataSource);
    }
}
