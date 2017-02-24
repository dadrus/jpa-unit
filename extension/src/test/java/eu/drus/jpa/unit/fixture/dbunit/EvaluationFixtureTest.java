package eu.drus.jpa.unit.fixture.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.dbunit.database.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.fixture.dbunit.EvaluationFixture;
import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationFixtureTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DatabaseConnection connection;

    @Mock
    private FeatureResolver resolver;

    @Before
    public void prepareMocks() throws Throwable {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(ctx.openConnection()).thenReturn(connection);
        when(ctx.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
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
        final EvaluationFixture fixture = new EvaluationFixture();

        // WHEN
        fixture.apply(invocation);

        // THEN
        order.verify(ctx).openConnection();
        order.verify(resolver).shouldCleanupBefore();
        order.verify(resolver).shouldCleanupUsingScriptBefore();
        order.verify(resolver).shouldApplyCustomScriptBefore();
        order.verify(resolver).shouldSeedData();
        order.verify(invocation).proceed();
        order.verify(resolver).shouldVerifyDataAfter();
        order.verify(resolver).shouldCleanupAfter();
        order.verify(resolver).shouldCleanupUsingScriptAfter();
        order.verify(resolver).shouldApplyCustomScriptAfter();
        order.verify(connection).close();
    }

    @Test
    public void testDataVerificationIsSkippedButAllAfterTestFeaturesAreExecutedIfInvocationProcessingFails() throws Throwable {
        // GIVEN
        final InOrder order = inOrder(ctx, invocation, resolver, connection);
        final EvaluationFixture fixture = new EvaluationFixture();

        doThrow(new Exception()).when(invocation).proceed();

        // WHEN
        try {
            fixture.apply(invocation);
        } catch (final Exception e) {

        }

        // THEN
        order.verify(ctx).openConnection();
        order.verify(resolver).shouldCleanupBefore();
        order.verify(resolver).shouldCleanupUsingScriptBefore();
        order.verify(resolver).shouldApplyCustomScriptBefore();
        order.verify(resolver).shouldSeedData();
        order.verify(invocation).proceed();
        order.verify(resolver, times(0)).shouldVerifyDataAfter();
        order.verify(resolver).shouldCleanupAfter();
        order.verify(resolver).shouldCleanupUsingScriptAfter();
        order.verify(resolver).shouldApplyCustomScriptAfter();
        order.verify(connection).close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final EvaluationFixture fixture = new EvaluationFixture();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(3));
    }
}
