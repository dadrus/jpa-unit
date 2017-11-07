package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperation;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.spi.FeatureResolver;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbFeatureExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Mock
    private FeatureResolver featureResolver;

    @Mock
    private MongoDatabase connection;

    @Mock
    private CleanupStrategy cleanupStrategy;

    @Mock
    private DataSeedStrategy dataSeedStrategy;

    @Mock
    private CleanupStrategyExecutor<MongoDatabase, Document> cleanupStrategyExecutor;

    @Mock
    private MongoDbOperation operation;

    @Mock
    private ExpectedDataSets expectedDataSets;

    private MongoDbFeatureExecutor featureExecutor;

    @Before
    public void createMongoDbFeatureFactory() {
        featureExecutor = new MongoDbFeatureExecutor(featureResolver);
    }

    @Test
    public void testLoadDataSetsUsingAvailableFilePaths() {
        // GIVEN

        // WHEN
        final List<Document> dataSetList = featureExecutor.loadDataSets(Arrays.asList("test-data.json", "test-data.json"));

        // THEN
        assertNotNull(dataSetList);
        assertThat(dataSetList.size(), equalTo(2));

        final Document doc1 = dataSetList.get(0);
        assertNotNull(doc1);
        assertThat(doc1.entrySet().size(), equalTo(2));

        final Document doc2 = dataSetList.get(1);
        assertNotNull(doc2);
        assertThat(doc2.entrySet().size(), equalTo(2));

        assertThat(doc1, equalTo(doc2));
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
        final List<Document> initialDataSets = Arrays.asList(new Document());

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor.createCleanupFeature(cleanupStrategy, initialDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(cleanupStrategyExecutor).execute(eq(connection), eq(initialDataSets));
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingAvailableFilePaths() throws DbFeatureException {
        // GIVEN

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor
                .createApplyCustomScriptFeature(Arrays.asList("test-data.json", "test-data.json"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection, times(2)).runCommand(any(Bson.class));
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingEmpyFile() throws DbFeatureException, IOException {
        // GIVEN
        final File tmpFile = tmpFolder.newFile();

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList(tmpFile.getPath()));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection, never()).runCommand(any(Bson.class));
    }

    @Test(expected = DbFeatureException.class)
    public void testApplyCustomScriptFeatureExecutionUsingNotAvailableFilePaths() throws DbFeatureException {
        // GIVEN

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList("test-data1.json"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        // DbFeatureException is thrown
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSeedDataFeatureExecution() throws DbFeatureException {
        // GIVEN
        when(dataSeedStrategy.provide(any(DataSeedStrategy.StrategyProvider.class))).thenReturn(operation);

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor.createSeedDataFeature(dataSeedStrategy,
                Arrays.asList(new Document(), new Document()));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(operation).execute(eq(connection), any(Document.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVerifyDataAfterFeatureExecution() throws DbFeatureException {
        // GIVEN
        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        final MongoCursor<String> iterator = mock(MongoCursor.class);
        when(expectedDataSets.strict()).thenReturn(Boolean.FALSE);
        when(expectedDataSets.value()).thenReturn(new String[] {});
        when(expectedDataSets.orderBy()).thenReturn(new String[] {});
        when(expectedDataSets.excludeColumns()).thenReturn(new String[] {});
        when(connection.listCollectionNames()).thenReturn(collectionIterable);
        when(collectionIterable.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<MongoDatabase> feature = featureExecutor.createVerifyDataAfterFeature(expectedDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection).listCollectionNames();
        verifyNoMoreInteractions(connection);
    }
}
