package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperation;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperations;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DbFeatureException;

public class CleanupStrategyProviderIT {
    private static MongodForTestsFactory factory;

    @BeforeClass
    public static void setUpMongo() throws IOException {
        factory = MongodForTestsFactory.with(Version.Main.V3_4);
    }

    @AfterClass
    public static void tearDownMongo() {
        factory.shutdown();
    }

    private MongoClient mongoClient;
    private MongoDatabase connection;
    private Document initialDataSet;

    private CleanupStrategyProvider provider;

    @Before
    public void prepareTest() throws MongoException, IOException {
        mongoClient = factory.newMongo();
        connection = mongoClient.getDatabase(UUID.randomUUID().toString());

        initialDataSet = new DataSetLoaderProvider().jsonLoader().load(new File("src/test/resources/test-data.json"));

        final MongoDbOperation operation = MongoDbOperations.CLEAN_INSERT;
        operation.execute(connection, initialDataSet);

        connection.getCollection("JSON_COLLECTION_1")
                .insertOne(new Document().append("_id", 10).append("version", "Record 10 version").append("value_1", "Record 10 Value 1")
                        .append("value_2", "Record 10 Value 2").append("value_3", "Record 10 Value 3")
                        .append("value_4", "Record 10 Value 4").append("value_5", "Record 10 Value 5"));

        connection.getCollection("JSON_COLLECTION_3").insertOne(new Document().append("_id", 11).append("version", "Record 11 version")
                .append("value_1", "Record 11 Value 8").append("value_2", "Record 11 Value 9"));

        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(4l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));

        provider = new CleanupStrategyProvider();
    }

    @Test
    public void testStrictCleanupWithInitialDataSets() throws DbFeatureException {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet));

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(0l));
    }

    @Test
    public void testStrictCleanupWithInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(0l));
    }

    @Test
    public void testStrictCleanupWithoutInitialDataSets() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList());

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(0l));
    }

    @Test
    public void testStrictCleanupWithoutInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList(), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(0l));
    }

    @Test(expected = IllegalStateException.class)
    public void testStrictCleanupOnClosedConnection() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());
        mongoClient.close();

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList());
    }

    @Test
    public void testUsedRowsOnlyCleanupWithInitialDataSets() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet));

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedRowsOnlyCleanupWithInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedRowsOnlyCleanupWithoutInitialDataSets() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList());

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(4l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedRowsOnlyCleanupWithoutInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList(), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(4l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test(expected = IllegalStateException.class)
    public void testUsedRowsOnlyCleanupOnClosedConnection() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());
        mongoClient.close();

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet));
    }

    @Test
    public void testUsedTablesOnlyCleanupWithInitialDataSets() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet));

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedTablesOnlyCleanupWithInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(0l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedTablesOnlyCleanupWithoutInitialDataSets() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList());

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(4l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test
    public void testUsedTablesOnlyCleanupWithoutInitialDataSetsExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.execute(connection, Collections.emptyList(), "JSON_COLLECTION_2");

        // THEN
        assertThat(connection.getCollection("JSON_COLLECTION_1").count(), equalTo(4l));
        assertThat(connection.getCollection("JSON_COLLECTION_2").count(), equalTo(1l));
        assertThat(connection.getCollection("JSON_COLLECTION_3").count(), equalTo(1l));
    }

    @Test(expected = IllegalStateException.class)
    public void testUsedTablesOnlyCleanupOnClosedConnection() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor<MongoDatabase, Document> strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());
        mongoClient.close();

        // WHEN
        strategyExecutor.execute(connection, Arrays.asList(initialDataSet));
    }
}
