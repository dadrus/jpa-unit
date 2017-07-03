package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

@RunWith(MockitoJUnitRunner.class)
public class SqlDbFeatureExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Mock
    private FeatureResolver featureResolver;

    @Mock
    private IDatabaseConnection connection;

    @Mock
    private Connection dbConnection;

    @Mock
    private Statement statement;

    @Mock
    private CleanupStrategy cleanupStrategy;

    @Mock
    private DataSeedStrategy dataSeedStrategy;

    @Mock
    private CleanupStrategyExecutor<IDatabaseConnection, IDataSet> cleanupStrategyExecutor;

    @Mock
    private DatabaseOperation operation;

    @Mock
    private ExpectedDataSets expectedDataSets;

    private SqlDbFeatureExecutor featureExecutor;

    @Before
    public void prepareTest() throws SQLException {
        featureExecutor = new SqlDbFeatureExecutor(featureResolver);

        when(connection.getConnection()).thenReturn(dbConnection);
        when(dbConnection.createStatement()).thenReturn(statement);
    }

    @Test
    public void testLoadDataSetsUsingAvailableFilePaths() throws DataSetException {
        // GIVEN

        // WHEN
        final List<IDataSet> dataSetList = featureExecutor.loadDataSets(Arrays.asList("test-data.json", "test-data.json"));

        // THEN
        assertNotNull(dataSetList);
        assertThat(dataSetList.size(), equalTo(2));

        final IDataSet ds1 = dataSetList.get(0);
        assertNotNull(ds1);
        assertThat(ds1.getTableNames().length, equalTo(2));

        final IDataSet ds2 = dataSetList.get(0);
        assertNotNull(ds2);
        assertThat(ds2.getTableNames().length, equalTo(2));

        assertThat(ds1, equalTo(ds2));
    }

    @Test(expected = JpaUnitException.class)
    public void testLoadDataSetsUsingNotAvailableFilePaths() {
        // GIVEN

        // WHEN
        featureExecutor.loadDataSets(Arrays.asList("test-data1.json"));

        // THEN
        // JpaUnitException is thrown
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCleanupFeatureExecution() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategy.provide(any(CleanupStrategy.StrategyProvider.class))).thenReturn(cleanupStrategyExecutor);
        final List<IDataSet> initialDataSets = Arrays.asList(mock(IDataSet.class));

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor.createCleanupFeature(cleanupStrategy, initialDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(cleanupStrategyExecutor).execute(eq(connection), eq(initialDataSets));
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingAvailableFilePaths() throws DbFeatureException, SQLException {
        // GIVEN

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor
                .createApplyCustomScriptFeature(Arrays.asList("test-data.json", "test-data.json"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(statement, times(2)).execute(anyString());
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingEmpyFile() throws DbFeatureException, IOException, SQLException {
        // GIVEN
        final File tmpFile = tmpFolder.newFile();

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList(tmpFile.getPath()));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(statement, never()).execute(anyString());
    }

    @Test(expected = DbFeatureException.class)
    public void testApplyCustomScriptFeatureExecutionUsingNotAvailableFilePaths() throws DbFeatureException {
        // GIVEN

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList("test-data1.json"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        // DbFeatureException is thrown
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSeedDataFeatureExecution() throws DbFeatureException, DatabaseUnitException, SQLException {
        // GIVEN
        final List<IDataSet> dataSets = featureExecutor.loadDataSets(Arrays.asList("test-data.json"));
        when(dataSeedStrategy.provide(any(DataSeedStrategy.StrategyProvider.class))).thenReturn(operation);

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor.createSeedDataFeature(dataSeedStrategy, dataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(operation).execute(eq(connection), any(IDataSet.class));
    }

    @Test
    public void testVerifyDataAfterFeatureExecution() throws DbFeatureException, SQLException, DataSetException {
        // GIVEN
        final IDataSet currentDs = mock(IDataSet.class);
        when(currentDs.getTableNames()).thenReturn(new String[] {});
        when(connection.createDataSet()).thenReturn(currentDs);
        when(expectedDataSets.strict()).thenReturn(Boolean.FALSE);
        when(expectedDataSets.value()).thenReturn(new String[] {});
        when(expectedDataSets.orderBy()).thenReturn(new String[] {});
        when(expectedDataSets.excludeColumns()).thenReturn(new String[] {});

        // WHEN
        final DbFeature<IDatabaseConnection> feature = featureExecutor.createVerifyDataAfterFeature(expectedDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection).createDataSet();
        verifyNoMoreInteractions(connection);
    }
}
