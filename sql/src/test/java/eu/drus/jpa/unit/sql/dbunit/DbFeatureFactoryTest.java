package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.sql.dbunit.CleanupStrategyProvider;
import eu.drus.jpa.unit.sql.dbunit.DataSeedStrategyProvider;
import eu.drus.jpa.unit.sql.dbunit.DbFeatureFactory;
import eu.drus.jpa.unit.sql.dbunit.StrategyProviderFactory;

@RunWith(MockitoJUnitRunner.class)
public class DbFeatureFactoryTest {

    @Mock
    private FeatureResolver resolver;

    @Mock
    private StrategyProviderFactory strategyProviderFactory;

    @Mock
    private CleanupStrategyProvider cleanupStrategyProvider;

    @Mock
    private CleanupStrategyExecutor<IDatabaseConnection, IDataSet> cleanupStrategyExecutor;

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
    public void testCleanUpBeforeIsDisabled() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanUpBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(0)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class), any(String[].class));
    }

    @Test
    public void testCleanUpBeforeIsEnabled() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanUpBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(1)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class));
    }

    @Test
    public void testCleanUpBeforeThrowsExceptionOnBadInitialDataSets() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Arrays.asList("not-there.xml"));
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        try {
            factory.getCleanUpBeforeFeature();
            fail("JpaUnitException expected");
        } catch (final JpaUnitException e) {
            // THEN
        }
    }

    @Test
    public void testCleanUpAfterIsDisabled() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanUpAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(0)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class), any(String[].class));
    }

    @Test
    public void testCleanUpAfterIsEnabled() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanUpAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(cleanupStrategyExecutor, times(1)).execute(any(DatabaseConnection.class), anyListOf(IDataSet.class));
    }

    @Test
    public void testCleanUpAfterThrowsExceptionOnBadInitialDataSets() throws Exception {
        // GIVEN
        when(cleanupStrategyProvider.strictStrategy()).thenReturn(cleanupStrategyExecutor);
        when(strategyProviderFactory.createCleanupStrategyProvider()).thenReturn(cleanupStrategyProvider);
        when(resolver.getSeedData()).thenReturn(Arrays.asList("not-there.xml"));
        when(resolver.getCleanupStrategy()).thenReturn(CleanupStrategy.STRICT);
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        try {
            factory.getCleanUpAfterFeature();
            fail("JpaUnitException expected");
        } catch (final JpaUnitException e) {
            // THEN
        }
    }

    @Test
    public void testCleanupUsingScriptBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanupUsingScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testCleanupUsingScriptBeforeIsEnabled() throws Exception {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getCleanupScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanupUsingScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement, times(3)).execute(contains("create table"));
    }

    @Test
    public void testCleanupUsingScriptAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanupUsingScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testCleanupUsingScriptAfterIsEnabled() throws Exception {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getCleanupScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getCleanupUsingScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement, times(3)).execute(contains("create table"));
    }

    @Test
    public void testApplyCustomScriptBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testApplyCustomScriptBeforeIsEnabled() throws Exception {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPreExecutionScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptBeforeFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement, times(3)).execute(contains("create table"));
    }

    @Test
    public void testApplyCustomScriptBeforeIsEnabledAndFails() throws Exception {
        // GIVEN
        final SQLException error = new SQLException("Could not execute statement");
        when(statement.execute(any(String.class))).thenThrow(error);
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPreExecutionScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptBeforeFeature();
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
    public void testApplyCustomScriptAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testApplyCustomScriptAfterIsEnabled() throws Exception {
        // GIVEN
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPostExecutionScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(statement, times(3)).execute(contains("create table"));
    }

    @Test
    public void testApplyCustomScriptAfterIsEnabledAndFails() throws Exception {
        // GIVEN
        final SQLException error = new SQLException("Could not execute statement");
        when(statement.execute(any(String.class))).thenThrow(error);
        when(connection.createStatement()).thenReturn(statement);
        when(dbUnitConnection.getConnection()).thenReturn(connection);
        when(resolver.getPostExecutionScripts()).thenReturn(Arrays.asList("schema.sql"));
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.TRUE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getApplyCustomScriptAfterFeature();
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
    public void testSeedDataIdDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldSeedData()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getSeedDataFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testSeedDataIsEnabled() throws Exception {
        // GIVEN
        when(resolver.getDataSeedStrategy()).thenReturn(DataSeedStrategy.INSERT);
        when(dataSeedStrategyProvider.insertStrategy()).thenReturn(databaseOperation);
        when(strategyProviderFactory.createDataSeedStrategyProvider()).thenReturn(dataSeedStrategyProvider);
        when(resolver.shouldSeedData()).thenReturn(Boolean.TRUE);
        when(resolver.getSeedData()).thenReturn(Arrays.asList("test-data.json"));
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getSeedDataFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(databaseOperation).execute(any(DatabaseConnection.class), any(IDataSet.class));
    }

    @Test
    public void testSeedDataThrowsExceptionOnBadInitialDataSets() throws Exception {
        // GIVEN
        when(resolver.getDataSeedStrategy()).thenReturn(DataSeedStrategy.INSERT);
        when(dataSeedStrategyProvider.insertStrategy()).thenReturn(databaseOperation);
        when(strategyProviderFactory.createDataSeedStrategyProvider()).thenReturn(dataSeedStrategyProvider);
        when(resolver.shouldSeedData()).thenReturn(Boolean.TRUE);
        when(resolver.getSeedData()).thenReturn(Arrays.asList("not-there.json"));
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);
        factory.setProviderFactory(strategyProviderFactory);

        // WHEN
        try {
            factory.getSeedDataFeature();
            fail("JpaUnitException expected");
        } catch (final JpaUnitException e) {
            // THEN
        }
    }

    @Test
    public void testVerifyDataAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.FALSE);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getVerifyDataAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        verify(dbUnitConnection, times(0)).getConnection();
    }

    @Test
    public void testVerifyDataAfterIsEnabled() throws Exception {
        // GIVEN
        final ExpectedDataSets eds = mock(ExpectedDataSets.class);
        when(eds.strict()).thenReturn(Boolean.FALSE);
        when(eds.orderBy()).thenReturn(new String[] {});
        when(eds.excludeColumns()).thenReturn(new String[] {});
        when(eds.value()).thenReturn(new String[] {});
        when(resolver.getExpectedDataSets()).thenReturn(eds);
        when(resolver.getCustomColumnFilter()).thenReturn(Collections.emptySet());
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.TRUE);

        final IDataSet ds = mock(IDataSet.class);
        when(ds.getTableNames()).thenReturn(new String[] {});
        when(dbUnitConnection.createDataSet()).thenReturn(ds);
        final DbFeatureFactory factory = new DbFeatureFactory(resolver);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = factory.getVerifyDataAfterFeature();
        assertThat(feature, notNullValue());

        feature.execute(dbUnitConnection);

        // THEN
        // no errors reported
    }
}
