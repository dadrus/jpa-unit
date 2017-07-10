package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        DatabaseConnectionFactory.class, DbUnitDecorator.class, DbUnitDecoratorTest.class
})
public class DbUnitDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DatabaseConnection connection;

    @Mock
    private SqlDbFeatureExecutor executor;

    private DbUnitDecorator decorator;

    @Before
    public void prepareMocks() throws Throwable {
        mockStatic(DatabaseConnectionFactory.class);
        when(DatabaseConnectionFactory.openConnection(any(BasicDataSource.class))).thenReturn(connection);
        whenNew(FeatureResolver.class).withAnyArguments().thenReturn(null);
        whenNew(SqlDbFeatureExecutor.class).withAnyArguments().thenReturn(executor);

        when(invocation.getContext()).thenReturn(ctx);
        when(ctx.getData(eq(DbUnitDecorator.KEY_CONNECTION))).thenReturn(connection);

        decorator = new DbUnitDecorator();
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.beforeTest(invocation);

        // THEN
        verify(executor).executeBeforeTest(eq(connection));
        verify(ctx).storeData(eq(DbUnitDecorator.KEY_CONNECTION), eq(connection));
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN
        when(invocation.hasErrors()).thenReturn(Boolean.FALSE);

        // WHEN
        decorator.afterTest(invocation);

        // THEN
        verify(executor).executeAfterTest(eq(connection), eq(Boolean.FALSE));
        verify(ctx).storeData(eq(DbUnitDecorator.KEY_CONNECTION), isNull());
    }

    @Test
    public void testDataVerificationIsSkippedButAllAfterTestFeaturesAreExecutedIfInvocationProcessingFails() throws Throwable {
        // GIVEN
        when(invocation.hasErrors()).thenReturn(Boolean.TRUE);
        doThrow(RuntimeException.class).when(executor).executeAfterTest(any(IDatabaseConnection.class), anyBoolean());

        // WHEN
        try {
            decorator.afterTest(invocation);
            fail("Exception expected");
        } catch (final Exception e) {
            // expected
        }

        // THEN
        verify(connection).close();
    }

    @Test
    public void testProcessInstanceDoesNotHaveAnyEffect() throws Exception {
        // GIVEN

        // WHEN
        decorator.processInstance(this, invocation);

        // THEN
        verifyNoMoreInteractions(invocation, connection, ctx, executor);
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
