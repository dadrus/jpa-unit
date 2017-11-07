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

import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.ExecutionContext;

@RunWith(MockitoJUnitRunner.class)
public class TestMethodStatementTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DecoratorExecutor jpaUnit;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    private TestMethodStatement statement;

    @Before
    public void setUp() {
        statement = new TestMethodStatement(ctx, jpaUnit, base, method, this);
        assertFalse(statement.getException().isPresent());
    }

    @Test
    public void testSuccessfulStatementEvaluation() throws Throwable {
        // GIVEN

        // WHEN
        statement.evaluate();

        // THEN
        final InOrder inOrder = inOrder(jpaUnit, base);
        inOrder.verify(jpaUnit).processBefore(eq(statement));
        inOrder.verify(base).evaluate();
        inOrder.verify(jpaUnit).processAfter(eq(statement));

        assertFalse(statement.getException().isPresent());
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
        final InOrder inOrder = inOrder(jpaUnit, base);
        inOrder.verify(jpaUnit).processBefore(eq(statement));
        inOrder.verify(base).evaluate();
        inOrder.verify(jpaUnit).processAfter(eq(statement));

        assertTrue(statement.getException().isPresent());
    }
}
