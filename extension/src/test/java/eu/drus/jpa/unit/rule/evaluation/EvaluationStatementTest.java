package eu.drus.jpa.unit.rule.evaluation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.dbunit.database.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.core.dbunit.DatabaseConnectionFactory;
import eu.drus.jpa.unit.core.dbunit.DbFeature;
import eu.drus.jpa.unit.core.dbunit.DbFeatureFactory;
import eu.drus.jpa.unit.rule.evaluation.EvaluationStatement;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationStatementTest {

    @Mock
    private DatabaseConnectionFactory connectionFactory;

    @Mock
    private DatabaseConnection connection;

    @Mock
    private DbFeatureFactory featureFactory;

    @Mock
    private DbFeature dbFeature;

    @Mock
    private Statement baseStatement;

    @InjectMocks
    private EvaluationStatement stmt;

    @Before
    public void prepareMocks() throws Throwable {
        when(connectionFactory.openConnection()).thenReturn(connection);
        when(featureFactory.getCleanUpBeforeFeature()).thenReturn(dbFeature);
        when(featureFactory.getCleanupUsingScriptBeforeFeature()).thenReturn(dbFeature);
        when(featureFactory.getApplyCustomScriptBeforeFeature()).thenReturn(dbFeature);
        when(featureFactory.getSeedDataFeature()).thenReturn(dbFeature);
        when(featureFactory.getVerifyDataAfterFeature()).thenReturn(dbFeature);
        when(featureFactory.getCleanUpAfterFeature()).thenReturn(dbFeature);
        when(featureFactory.getCleanupUsingScriptAfterFeature()).thenReturn(dbFeature);
        when(featureFactory.getApplyCustomScriptAfterFeature()).thenReturn(dbFeature);
    }

    @Test
    public void testStatementEvaluation() throws Throwable {
        // GIVEN
        final InOrder order = inOrder(connectionFactory, baseStatement, featureFactory, connection);

        // WHEN
        stmt.evaluate();

        // THEN
        order.verify(connectionFactory).openConnection();
        order.verify(featureFactory).getCleanUpBeforeFeature();
        order.verify(featureFactory).getCleanupUsingScriptBeforeFeature();
        order.verify(featureFactory).getApplyCustomScriptBeforeFeature();
        order.verify(featureFactory).getSeedDataFeature();
        order.verify(baseStatement).evaluate();
        order.verify(featureFactory).getVerifyDataAfterFeature();
        order.verify(featureFactory).getCleanUpAfterFeature();
        order.verify(featureFactory).getCleanupUsingScriptAfterFeature();
        order.verify(featureFactory).getApplyCustomScriptAfterFeature();
        verify(dbFeature, times(8)).execute(eq(connection));
        order.verify(connection).close();
    }

    @Test
    public void testDataVerificationIsSkippedButAllAfterTestFeaturesAreExecutedIfBaseStatementEvaluationFails() throws Throwable {
        // GIVEN
        final InOrder order = inOrder(connectionFactory, baseStatement, featureFactory, connection);
        doThrow(new Exception()).when(baseStatement).evaluate();

        // WHEN
        try {
            stmt.evaluate();
        } catch (final Exception e) {

        }

        // THEN
        order.verify(connectionFactory).openConnection();
        order.verify(featureFactory).getCleanUpBeforeFeature();
        order.verify(featureFactory).getCleanupUsingScriptBeforeFeature();
        order.verify(featureFactory).getApplyCustomScriptBeforeFeature();
        order.verify(featureFactory).getSeedDataFeature();
        order.verify(baseStatement).evaluate();
        order.verify(featureFactory, times(0)).getVerifyDataAfterFeature();
        order.verify(featureFactory).getCleanUpAfterFeature();
        order.verify(featureFactory).getCleanupUsingScriptAfterFeature();
        order.verify(featureFactory).getApplyCustomScriptAfterFeature();
        verify(dbFeature, times(7)).execute(eq(connection));
        order.verify(connection).close();
    }
}
