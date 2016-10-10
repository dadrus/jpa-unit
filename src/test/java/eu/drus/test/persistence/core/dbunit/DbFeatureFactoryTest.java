package eu.drus.test.persistence.core.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.annotation.CleanupStrategy;
import eu.drus.test.persistence.annotation.DataSeedStrategy;
import eu.drus.test.persistence.core.metadata.FeatureResolver;

@RunWith(MockitoJUnitRunner.class)
public class DbFeatureFactoryTest {

    @Mock
    private FeatureResolver resolver;

    @Mock
    private StrategyProviderFactory strategyProviderFactory;

    @Mock
    private CleanupStrategyProvider cleanupStrategyProvider;

    @Mock
    private CleanupStrategyExecutor cleanupStrategyExecutor;

    @Mock
    private DataSeedStrategyProvider dataSeedStrategyProvider;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private DatabaseConnection dbUnitConnection;

    @Mock
    private DatabaseOperation databaseOperation;

    @Test
    public void testCleanUpBeforeIsDisabled() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanUpBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(0)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class), any(String[].class));
    }

    @Test
    public void testCleanUpBeforeIsEnabled() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature feature = factory.getCleanUpBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(1)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class));
    }

    @Test
    public void testCleanUpAfterIsDisabled() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanUpAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(0)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class), any(String[].class));
    }

    @Test
    public void testCleanUpAfterIsEnabled() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature feature = factory.getCleanUpAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(1)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class));
    }

    @Test
    public void testCleanupUsingScriptBeforeIsDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanupUsingScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testCleanupUsingScriptBeforeIsEnabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getCleanupScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanupUsingScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement).execute(any(String.class));
    }

    @Test
    public void testCleanupUsingScriptAfterIsDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanupUsingScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testCleanupUsingScriptAfterIsEnabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getCleanupScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getCleanupUsingScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement).execute(any(String.class));
    }

    @Test
    public void testApplyCustomScriptBeforeIsDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testApplyCustomScriptBeforeIsEnabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPreExecutionScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement).execute(any(String.class));
    }

    @Test
    public void testApplyCustomScriptBeforeIsEnabledAndFails() throws DbFeatureException, SQLException {
        // GIVEN
        final SQLException error = new SQLException("Could not execute statement");
        when(statement.execute(any(String.class))).thenThrow(error);
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPreExecutionScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptBeforeFeature();
        assertThat(feature, notNullValue());

        try {
            feature.execute(dbUnitConnection);
            fail("DbFeatureException expected");
        } catch (final DbFeatureException e) {

            // THEN
            assertThat(e.getCause(), equalTo(error));
        }
    }

    @Test
    public void testApplyCustomScriptAfterIsDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testApplyCustomScriptAfterIsEnabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPostExecutionScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement).execute(any(String.class));
    }

    @Test
    public void testApplyCustomScriptAfterIsEnabledAndFails() throws DbFeatureException, SQLException {
        // GIVEN
        final SQLException error = new SQLException("Could not execute statement");
        when(statement.execute(any(String.class))).thenThrow(error);
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPostExecutionScripts()).thenReturn(Arrays.asList("src/test/resources/schema.sql"));
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getApplyCustomScriptAfterFeature();
        assertThat(feature, notNullValue());

        try {
            feature.execute(dbUnitConnection);
            fail("DbFeatureException expected");
        } catch (final DbFeatureException e) {

            // THEN
            assertThat(e.getCause(), equalTo(error));
        }
    }

    @Test
    public void testSeedDataIdDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldSeedData()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getSeedDataFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testSeedDataIsEnabled() throws DbFeatureException, SQLException, DatabaseUnitException {
        // GIVEN
        when(resolver.getDataSeedStrategy()).thenReturn(DataSeedStrategy.INSERT);
        when(dataSeedStrategyProvider.insertStrategy()).thenReturn(databaseOperation);
        when(strategyProviderFactory.createDataSeedStrategyProvider()).thenReturn(dataSeedStrategyProvider);
        when(resolver.shouldSeedData()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature feature = factory.getSeedDataFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(databaseOperation).execute(any(DatabaseConnection.class), any(IDataSet.class));
    }

    @Test
    public void testVerifyDataAfterIsDisabled() throws DbFeatureException, SQLException {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature feature = factory.getVerifyDataAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    public void testVerifyDataAfterIsEnabled() throws DbFeatureException, SQLException {
        // TODO
    }
}
