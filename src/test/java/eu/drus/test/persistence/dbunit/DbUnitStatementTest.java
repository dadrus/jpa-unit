package eu.drus.test.persistence.dbunit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.dbunit.database.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbUnitStatementTest {

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
    private DbUnitStatement stmt;

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

        // WHEN
        stmt.evaluate();

        // THEN
        verify(connectionFactory).openConnection();
        verify(baseStatement).evaluate();
        verify(featureFactory).getCleanUpBeforeFeature();
        verify(featureFactory).getCleanupUsingScriptBeforeFeature();
        verify(featureFactory).getApplyCustomScriptBeforeFeature();
        verify(featureFactory).getSeedDataFeature();
        verify(featureFactory).getVerifyDataAfterFeature();
        verify(featureFactory).getCleanUpAfterFeature();
        verify(featureFactory).getCleanupUsingScriptAfterFeature();
        verify(featureFactory).getApplyCustomScriptAfterFeature();
        verify(dbFeature, times(8)).execute(eq(connection));
        verify(connection).close();
    }

    @Test
    public void testDataVerificationIsSkippedButAllAfterTestFeaturesAreExecutedIfBaseStatementEvaluationFails() throws Throwable {
        // GIVEN
        doThrow(new Exception()).when(baseStatement).evaluate();

        // WHEN
        try {
            stmt.evaluate();
        } catch (final Exception e) {

        }

        // THEN
        verify(connectionFactory).openConnection();
        verify(baseStatement).evaluate();
        verify(featureFactory).getCleanUpBeforeFeature();
        verify(featureFactory).getCleanupUsingScriptBeforeFeature();
        verify(featureFactory).getApplyCustomScriptBeforeFeature();
        verify(featureFactory).getSeedDataFeature();
        verify(featureFactory, times(0)).getVerifyDataAfterFeature();
        verify(featureFactory).getCleanUpAfterFeature();
        verify(featureFactory).getCleanupUsingScriptAfterFeature();
        verify(featureFactory).getApplyCustomScriptAfterFeature();
        verify(dbFeature, times(7)).execute(eq(connection));
        verify(connection).close();
    }
}
