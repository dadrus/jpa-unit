package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Optional;

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
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.sql.Constants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        DbUnitTestMethodDecorator.class, DbUnitTestMethodDecoratorTest.class
})
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
        whenNew(FeatureResolver.class).withAnyArguments().thenReturn(null);
        whenNew(SqlDbFeatureExecutor.class).withAnyArguments().thenReturn(executor);

        when(invocation.getContext()).thenReturn(ctx);
        when(ctx.getData(eq(Constants.KEY_CONNECTION))).thenReturn(connection);
        when(ctx.getData(eq(Constants.KEY_FEATURE_EXECUTOR))).thenReturn(executor);

        decorator = new DbUnitTestMethodDecorator();
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.beforeTest(invocation);

        // THEN
        verify(executor).executeBeforeTest(eq(connection));
        verify(ctx, times(0)).storeData(eq(Constants.KEY_CONNECTION), any(IDatabaseConnection.class));
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
        verifyZeroInteractions(connection);
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
