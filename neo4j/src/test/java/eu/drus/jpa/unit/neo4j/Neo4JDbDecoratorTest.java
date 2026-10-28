package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class Neo4JDbDecoratorTest {

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private Neo4JDbDecorator decorator;

    @Before
    public void prepareTest() throws Exception {
        try (final MockedConstruction<ConfigurationRegistry> constructions = mockConstruction(ConfigurationRegistry.class)) {
            decorator = new Neo4JDbDecorator();
        }

        lenient().when(invocation.getContext()).thenReturn(ctx);
        lenient().when(invocation.getException()).thenReturn(Optional.empty());
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(ctx.getData(eq(Constants.KEY_DATA_SOURCE))).thenReturn(dataSource);
        lenient().when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        lenient().when(ctx.getDescriptor()).thenReturn(descriptor);
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
        try (final MockedConstruction<Neo4JDbFeatureExecutor> constructions = mockConstruction(Neo4JDbFeatureExecutor.class)) {
            decorator.beforeTest(invocation);

            // THEN
            final Neo4JDbFeatureExecutor dbFeatureExecutor = constructions.constructed().get(0);
            verify(connection).setAutoCommit(eq(Boolean.FALSE));
            verify(dbFeatureExecutor).executeBeforeTest(eq(connection));
            verify(ctx).storeData(eq(Constants.KEY_CONNECTION), eq(connection));
        }
    }

    @Test
    public void testAfterTestExecutesWithoutErrors() throws Exception {
        // GIVEN

        // WHEN
        try (final MockedConstruction<Neo4JDbFeatureExecutor> constructions = mockConstruction(Neo4JDbFeatureExecutor.class)) {
            decorator.afterTest(invocation);

            // THEN
            final Neo4JDbFeatureExecutor dbFeatureExecutor = constructions.constructed().get(0);
            verify(connection).close();
            verify(dbFeatureExecutor).executeAfterTest(eq(connection), eq(Boolean.FALSE));
            verify(ctx).storeData(eq(Constants.KEY_CONNECTION), isNull());
            verifyNoInteractions(dataSource);
        }
    }

    @Test
    public void testAfterTestExecutesWithErrors() throws Exception {
        // GIVEN
        try (final MockedConstruction<Neo4JDbFeatureExecutor> constructions = mockConstruction(Neo4JDbFeatureExecutor.class,
                (mock, context) -> doThrow(RuntimeException.class).when(mock).executeAfterTest(eq(connection), eq(Boolean.FALSE)))) {

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
            verifyNoInteractions(dataSource);
        }
    }
}
