package eu.drus.jpa.unit.rule;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;

@RunWith(MockitoJUnitRunner.class)
public class TestClassStatementTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private Statement base;

    @Mock
    private DecoratorExecutor jpaUnit;

    private TestClassStatement statement;

    private Map<String, Object> map = new HashMap<>();

    @Before
    public void setUp() {
        doAnswer((final InvocationOnMock invocation) -> {
            final String key = (String) invocation.getArguments()[0];
            return map.get(key);
        }).when(ctx).getData(anyString());

        doAnswer((final InvocationOnMock invocation) -> {
            final String key = (String) invocation.getArguments()[0];
            final Object value = invocation.getArguments()[1];
            return map.put(key, value);
        }).when(ctx).storeData(anyString(), anyObject());

        statement = new TestClassStatement(ctx, jpaUnit, base, this);
    }

    @Test
    public void testOnMultipleEvaluateCallsBeforeAllAndAfterAllIsCalledOnlyOnce() throws Throwable {
        // GIVEN

        // WHEN
        statement.evaluate();
        statement.evaluate();

        // THEN
        final InOrder inOrder = inOrder(jpaUnit, base);
        inOrder.verify(jpaUnit).processBeforeAll(eq(ctx), eq(this.getClass()));
        inOrder.verify(base, times(2)).evaluate();
        inOrder.verify(jpaUnit).processAfterAll(eq(ctx), eq(this.getClass()));
    }

    @Test
    public void testBeforeAllAndAfterAllIsCalledOnlyOnceEvenInCaseOfErrors() throws Throwable {
        // GIVEN
        doThrow(RuntimeException.class).when(base).evaluate();

        // WHEN
        try {
            statement.evaluate();
            fail("Exception expected");
        } catch (final RuntimeException e) {

        }
        try {
            statement.evaluate();
            fail("Exception expected");
        } catch (final RuntimeException e) {

        }

        // THEN
        final InOrder inOrder = inOrder(jpaUnit, base);
        inOrder.verify(jpaUnit).processBeforeAll(eq(ctx), eq(this.getClass()));
        inOrder.verify(base, times(2)).evaluate();
        inOrder.verify(jpaUnit).processAfterAll(eq(ctx), eq(this.getClass()));
    }
}
