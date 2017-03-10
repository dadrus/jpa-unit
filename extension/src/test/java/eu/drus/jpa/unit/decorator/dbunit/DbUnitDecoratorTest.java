package eu.drus.jpa.unit.decorator.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.lang.reflect.Method;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        DatabaseConnectionFactory.class, FeatureResolverFactory.class
})
public class DbUnitDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DatabaseConnection connection;

    @Mock
    private FeatureResolver resolver;

    @Before
    public void prepareMocks() throws Throwable {
        mockStatic(DatabaseConnectionFactory.class, FeatureResolverFactory.class);
        when(DatabaseConnectionFactory.openConnection(any(BasicDataSource.class))).thenReturn(connection);
        when(FeatureResolverFactory.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);

        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.FALSE);
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.FALSE);
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.FALSE);
        when(resolver.shouldSeedData()).thenReturn(Boolean.FALSE);
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.FALSE);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.FALSE);
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.FALSE);
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testApplyFixture() throws Throwable {
        // GIVEN
        final InOrder order = inOrder(ctx, invocation, resolver, connection);
        final DbUnitDecorator fixture = new DbUnitDecorator();

        // WHEN
        fixture.apply(invocation);

        // THEN
        order.verify(resolver).shouldCleanupBefore();
        order.verify(resolver).shouldCleanupUsingScriptBefore();
        order.verify(resolver).shouldApplyCustomScriptBefore();
        order.verify(resolver).shouldSeedData();
        order.verify(invocation).proceed();
        order.verify(resolver).shouldVerifyDataAfter();
        order.verify(resolver).shouldApplyCustomScriptAfter();
        order.verify(resolver).shouldCleanupUsingScriptAfter();
        order.verify(resolver).shouldCleanupAfter();
        order.verify(connection).close();
    }

    @Test
    public void testDataVerificationIsSkippedButAllAfterTestFeaturesAreExecutedIfInvocationProcessingFails() throws Throwable {
        // GIVEN
        final InOrder order = inOrder(ctx, invocation, resolver, connection);
        final DbUnitDecorator fixture = new DbUnitDecorator();

        doThrow(new Exception()).when(invocation).proceed();

        // WHEN
        try {
            fixture.apply(invocation);
        } catch (final Exception e) {

        }

        // THEN
        order.verify(resolver).shouldCleanupBefore();
        order.verify(resolver).shouldCleanupUsingScriptBefore();
        order.verify(resolver).shouldApplyCustomScriptBefore();
        order.verify(resolver).shouldSeedData();
        order.verify(invocation).proceed();
        order.verify(resolver, times(0)).shouldVerifyDataAfter();
        order.verify(resolver).shouldApplyCustomScriptAfter();
        order.verify(resolver).shouldCleanupUsingScriptAfter();
        order.verify(resolver).shouldCleanupAfter();
        order.verify(connection).close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final DbUnitDecorator fixture = new DbUnitDecorator();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(3));
    }
}
