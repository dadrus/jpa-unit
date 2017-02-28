package eu.drus.jpa.unit.cdi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(MockitoJUnitRunner.class)
public class CdiProducerDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private EntityManager em;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private EntityManagerHolder emh;

    @InjectMocks
    private CdiProducerDecorator fixture;

    @Before
    public void setUp() throws Exception {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(ctx.getData(eq("em"))).thenReturn(em);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(10));
    }

    @Test
    public void testApplyFixture() throws Throwable {
        // GIVEN

        // WHEN
        fixture.apply(invocation);

        // THEN
        final InOrder order = inOrder(emh, invocation);
        order.verify(emh).setEntityManager(eq(em));
        order.verify(invocation).proceed();
        order.verify(emh).setEntityManager(eq(null));
    }

    @Test
    public void testApplyFixtureWithErrorInProceedInvocation() throws Throwable {
        // GIVEN
        doThrow(new Exception("some reason")).when(invocation).proceed();

        // WHEN
        try {
            fixture.apply(invocation);
            fail("Exception expected");
        } catch (final Exception e) {
            // expected
        }

        // THEN
        final InOrder order = inOrder(emh, invocation);
        order.verify(emh).setEntityManager(eq(em));
        order.verify(invocation).proceed();
        order.verify(emh).setEntityManager(eq(null));
    }
}
