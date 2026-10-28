package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.sql.Constants;

@RunWith(MockitoJUnitRunner.class)
public class DbUnitDatabaseConnectionDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DatabaseConfig dbConfig;

    @Mock
    private IDatabaseConnection connection;

    @Mock
    private DatabaseConnectionFactory connectionFatory;

    @Mock
    private TestInvocation invocation;

    private MockedStatic<DatabaseConnectionFactory> connectionFactoryMock;

    private DbUnitDatabaseConnectionDecorator decorator;

    @Before
    public void prepareMocks() throws Throwable {
        connectionFactoryMock = mockStatic(DatabaseConnectionFactory.class);
        connectionFactoryMock.when(() -> DatabaseConnectionFactory.openConnection(nullable(BasicDataSource.class))).thenReturn(connection);

        when(connection.getConfig()).thenReturn(dbConfig);
        when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        when(invocation.getContext()).thenReturn(ctx);

        decorator = new DbUnitDatabaseConnectionDecorator();
    }

    @After
    public void releaseMocks() {
        connectionFactoryMock.close();
    }

    @Test
    public void testBeforeAll() throws Throwable {
        // GIVEN

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_CONNECTION), eq(connection));
    }

    @Test
    public void testAfterAll() throws Throwable {
        // GIVEN

        // WHEN
        decorator.afterAll(invocation);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_CONNECTION), isNull());
        verify(connection).close();
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
