package eu.drus.jpa.unit.decorator.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(MockitoJUnitRunner.class)
public class TransactionDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private EntityManager em;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private FeatureResolver resolver;

    @Before
    public void setUp() throws Exception {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getFeatureResolver()).thenReturn(resolver);
        when(resolver.getTransactionMode()).thenReturn(TransactionMode.DISABLED);
    }

    @Test
    public void testNoTransactionStrategyIsExecutedForEntityManagerFactory() throws Throwable {
        // GIVEN
        final TransactionDecorator fixture = new TransactionDecorator();

        // WHEN
        fixture.beforeTest(invocation);
        fixture.afterTest(invocation);

        // THEN
        verify(resolver, times(0)).getTransactionMode();
        verify(em, times(0)).clear();
    }

    @Test
    public void testTransactionStrategyIsExecutedForEntityManager() throws Throwable {
        // GIVEN
        when(ctx.getData(eq(Constants.KEY_ENTITY_MANAGER))).thenReturn(em);
        final TransactionDecorator fixture = new TransactionDecorator();

        // WHEN
        fixture.beforeTest(invocation);
        fixture.afterTest(invocation);

        // THEN
        verify(resolver, atLeastOnce()).getTransactionMode();
        verify(em).clear();
    }

    @Test
    public void testProcessInstanceDoesNotHaveAnyEffect() throws Exception {
        // GIVEN
        final TransactionDecorator fixture = new TransactionDecorator();

        // WHEN
        fixture.processInstance(this, invocation);

        // THEN
        verifyNoMoreInteractions(invocation, em, ctx, resolver);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final TransactionDecorator fixture = new TransactionDecorator();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(100));
    }
}
