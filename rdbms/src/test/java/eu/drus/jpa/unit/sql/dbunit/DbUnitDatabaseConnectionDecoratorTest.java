package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.sql.Constants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    DatabaseConnectionFactory.class
})
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

    private DbUnitDatabaseConnectionDecorator decorator;

    @Before
    public void prepareMocks() throws Throwable {
        mockStatic(DatabaseConnectionFactory.class);

        when(connection.getConfig()).thenReturn(dbConfig);
        when(DatabaseConnectionFactory.openConnection(any(BasicDataSource.class))).thenReturn(connection);
        when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        when(invocation.getContext()).thenReturn(ctx);

        decorator = new DbUnitDatabaseConnectionDecorator();
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
