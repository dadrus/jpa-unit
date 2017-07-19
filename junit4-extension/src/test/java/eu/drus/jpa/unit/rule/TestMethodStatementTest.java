package eu.drus.jpa.unit.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

@RunWith(MockitoJUnitRunner.class)
public class TestMethodStatementTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private TestMethodDecorator decorator;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    private TestMethodStatement statement;

    @Before
    public void setUp() {
        statement = new TestMethodStatement(ctx, decorator, base, method, this);
        assertFalse(statement.hasErrors());
    }

    @Test
    public void testSuccessfulStatementEvaluation() throws Throwable {
        // GIVEN

        // WHEN
        statement.evaluate();

        // THEN
        final InOrder inOrder = inOrder(decorator, base);
        inOrder.verify(decorator).beforeTest(eq(statement));
        inOrder.verify(base).evaluate();
        inOrder.verify(decorator).afterTest(eq(statement));

        assertFalse(statement.hasErrors());
    }

    @Test
    public void testErroneousfulStatementEvaluation() throws Throwable {
        // GIVEN
        doThrow(RuntimeException.class).when(base).evaluate();

        // WHEN
        try {
            statement.evaluate();
            fail("Exception expected");
        } catch (final RuntimeException e) {

        }

        // THEN
        final InOrder inOrder = inOrder(decorator, base);
        inOrder.verify(decorator).beforeTest(eq(statement));
        inOrder.verify(base).evaluate();
        inOrder.verify(decorator).afterTest(eq(statement));

        assertTrue(statement.hasErrors());
    }
}
