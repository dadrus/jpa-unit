package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.sql.Constants;

@RunWith(MockitoJUnitRunner.class)
public class DbUnitTestMethodDecoratorTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DatabaseConnection connection;

    @Mock
    private SqlDbFeatureExecutor executor;

    private DbUnitTestMethodDecorator decorator;

    @Before
    public void prepareMocks() throws Throwable {
        when(invocation.getContext()).thenReturn(ctx);
        when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        when(ctx.getData(eq(Constants.KEY_FEATURE_EXECUTOR))).thenReturn(executor);

        decorator = new DbUnitTestMethodDecorator();
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN
        try (MockedConstruction<SqlDbFeatureExecutor> executorConstruction = mockConstruction(SqlDbFeatureExecutor.class)) {
            // WHEN
            decorator.beforeTest(invocation);

            // THEN
            verify(executorConstruction.constructed().get(0)).executeBeforeTest(eq(connection));
            verify(ctx, times(0)).storeData(eq(Constants.KEY_CONNECTION), any(IDatabaseConnection.class));
        }
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN
        when(invocation.getException()).thenReturn(Optional.empty());

        // WHEN
        decorator.afterTest(invocation);

        // THEN
        verify(executor).executeAfterTest(eq(connection), eq(Boolean.FALSE));
        verify(ctx, times(0)).storeData(eq(Constants.KEY_CONNECTION), any(IDatabaseConnection.class));
        verifyNoInteractions(connection);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(3));
    }
}
