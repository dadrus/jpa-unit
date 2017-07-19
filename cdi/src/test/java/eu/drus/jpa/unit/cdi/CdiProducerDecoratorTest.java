package eu.drus.jpa.unit.cdi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.Constants;
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
        when(ctx.getData(eq(Constants.KEY_ENTITY_MANAGER))).thenReturn(em);
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
    public void testBeforeInstance() throws Exception {
        // GIVEN

        // WHEN
        fixture.beforeTest(invocation);

        // THEN
        verify(emh).setEntityManager(eq(em));
        verifyNoMoreInteractions(emh, em);
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN

        // WHEN
        fixture.afterTest(invocation);

        // THEN
        verify(emh).setEntityManager(eq(null));
        verifyNoMoreInteractions(invocation, emh, em);
    }
}
