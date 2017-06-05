package eu.drus.jpa.unit.mongodb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.AbstractDbFeatureFactory;
import eu.drus.jpa.unit.core.AssertionErrorCollector;
import eu.drus.jpa.unit.core.CleanupStrategyExecutor;
import eu.drus.jpa.unit.core.DataSetFormat;
import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.core.DbFeature;
import eu.drus.jpa.unit.core.DbFeatureException;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperation;

public class MongoDbFeatureFactory extends AbstractDbFeatureFactory<Document, MongoDatabase> {

    public MongoDbFeatureFactory(final FeatureResolver featureResolver) {
        super(featureResolver);
    }

    private static Document mergeDataSets(final Iterable<Document> dataSetList) {
        final Document dataSet = new Document();
        for (final Document doc : dataSetList) {
            doc.forEach(dataSet::put);
        }
        return dataSet;
    }

    private static URI toUri(final String path) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new JpaUnitException(path + " not found");
        }

        try {
            return url.toURI();
        } catch (final URISyntaxException e) {
            throw new JpaUnitException("Could not convert " + path + " to URI.", e);
        }
    }

    @Override
    protected List<Document> loadDataSets(final List<String> paths) {
        final List<Document> dataSets = new ArrayList<>();
        try {
            for (final String path : paths) {
                final File file = new File(toUri(path));
                final DataSetLoader<Document> loader = DataSetFormat.inferFromFile(file).select(new DataSetLoaderProvider());
                dataSets.add(loader.load(file));
            }
        } catch (final IOException e) {
            throw new JpaUnitException("Could not load initial data sets", e);
        }
        return dataSets;
    }

    @Override
    protected DbFeature<MongoDatabase> createCleanupFeature(final CleanupStrategy cleanupStrategy, final List<Document> initialDataSets) {
        return (final MongoDatabase connection) -> {
            final CleanupStrategyExecutor<MongoDatabase, Document> executor = cleanupStrategy.provide(new CleanupStrategyProvider());
            executor.execute(connection, initialDataSets);
        };
    }

    @Override
    protected DbFeature<MongoDatabase> createApplyCustomScriptFeature(final List<String> scriptPaths) {
        return (final MongoDatabase connection) -> {
            try {
                for (final String scriptPath : scriptPaths) {
                    executeScript(loadScript(scriptPath), connection);
                }
            } catch (IOException | URISyntaxException e) {
                throw new DbFeatureException("Could not apply custom scripts feature", e);
            }
        };
    }

    @Override
    protected DbFeature<MongoDatabase> createSeedDataFeature(final DataSeedStrategy dataSeedStrategy,
            final List<Document> initialDataSets) {
        return (final MongoDatabase connection) -> {
            final MongoDbOperation operation = dataSeedStrategy.provide(new DataSeedStrategyProvider());
            operation.execute(connection, mergeDataSets(initialDataSets));
        };
    }

    @Override
    protected DbFeature<MongoDatabase> createVerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
        return (final MongoDatabase connection) -> {
            final Document expectedDataSet = mergeDataSets(loadDataSets(Arrays.asList(expectedDataSets.value())));

            final DataSetComparator dataSetComparator = new DataSetComparator(expectedDataSets.orderBy(), expectedDataSets.excludeColumns(),
                    expectedDataSets.strict());

            final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
            dataSetComparator.compare(connection, expectedDataSet, errorCollector);

            errorCollector.report();
        };
    }

    private void executeScript(final String script, final MongoDatabase connection) {
        final Document command = Document.parse(script);
        connection.runCommand(command);
    }
}
