package eu.drus.jpa.unit.fixture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.annotation.TransactionMode;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class TransactionFixtureTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private EntityManager em;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private FeatureResolver resolver;

    @Before
    public void setUp() throws Exception {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(ctx.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
        when(resolver.getTransactionMode()).thenReturn(TransactionMode.DISABLED);
    }

    @Test
    public void testNoTransactionStrategyIsExecutedForEntityManagerFactory() throws Throwable {
        // GIVEN
        final TransactionFixture fixture = new TransactionFixture();

        // WHEN
        fixture.apply(invocation);

        // THEN
        verify(invocation).proceed();
        verify(resolver, times(0)).getTransactionMode();
        verify(em, times(0)).clear();
    }

    @Test
    public void testTransactionStrategyIsExecutedForEntityManager() throws Throwable {
        // GIVEN
        when(ctx.getData(eq("em"))).thenReturn(em);
        final TransactionFixture fixture = new TransactionFixture();

        // WHEN
        fixture.apply(invocation);

        // THEN
        verify(invocation).proceed();
        verify(resolver).getTransactionMode();
        verify(em).clear();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final TransactionFixture fixture = new TransactionFixture();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(4));
    }
}
