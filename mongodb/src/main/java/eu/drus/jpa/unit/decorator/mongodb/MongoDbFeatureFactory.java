package eu.drus.jpa.unit.decorator.mongodb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.CleanupStrategyExecutor;
import eu.drus.jpa.unit.core.DataSetFormat;
import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.core.DbFeature;
import eu.drus.jpa.unit.core.DbFeatureException;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;

public class MongoDbFeatureFactory {

    private final FeatureResolver featureResolver;
    private List<Document> initialDataSets;

    public MongoDbFeatureFactory(final FeatureResolver featureResolver) {
        this.featureResolver = featureResolver;
    }

    private static List<Document> loadDataSets(final List<String> paths) {
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

    private List<Document> getInitialDataSets() {
        if (initialDataSets == null) {
            initialDataSets = loadDataSets(featureResolver.getSeedData());
        }
        return initialDataSets;
    }

    public DbFeature<MongoDatabase> getCleanUpBeforeFeature() {
        if (featureResolver.shouldCleanupBefore()) {
            return new CleanupFeature(new CleanupStrategyProvider(), featureResolver.getCleanupStrategy(), getInitialDataSets());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getCleanUpAfterFeature() {
        if (featureResolver.shouldCleanupAfter()) {
            return new CleanupFeature(new CleanupStrategyProvider(), featureResolver.getCleanupStrategy(), getInitialDataSets());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getCleanupUsingScriptBeforeFeature() {
        if (featureResolver.shouldCleanupUsingScriptBefore()) {
            return new ApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getCleanupUsingScriptAfterFeature() {
        if (featureResolver.shouldCleanupUsingScriptAfter()) {
            return new ApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getApplyCustomScriptBeforeFeature() {
        if (featureResolver.shouldApplyCustomScriptBefore()) {
            return new ApplyCustomScriptFeature(featureResolver.getPreExecutionScripts());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getApplyCustomScriptAfterFeature() {
        if (featureResolver.shouldApplyCustomScriptAfter()) {
            return new ApplyCustomScriptFeature(featureResolver.getPostExecutionScripts());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getSeedDataFeature() {
        if (featureResolver.shouldSeedData()) {
            return new SeedDataFeature(new DataSeedStrategyProvider(), featureResolver.getDataSeedStrategy(), getInitialDataSets());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    public DbFeature<MongoDatabase> getVerifyDataAfterFeature() {
        if (featureResolver.shouldVerifyDataAfter()) {
            return new VerifyDataAfterFeature(featureResolver.getExpectedDataSets());
        } else {
            return NopFeature.INSTANCE;
        }
    }

    private static class NopFeature implements DbFeature<MongoDatabase> {

        private static final NopFeature INSTANCE = new NopFeature();

        @Override
        public void execute(final MongoDatabase connection) throws DbFeatureException {
            // does nothing like the name implies
        }
    }

    private static class CleanupFeature implements DbFeature<MongoDatabase> {

        private final CleanupStrategy cleanupStrategy;
        private final List<Document> initialDataSets;
        private final CleanupStrategyProvider provider;

        private CleanupFeature(final CleanupStrategyProvider provider, final CleanupStrategy cleanupStrategy,
                final List<Document> initialDataSets) {
            this.provider = provider;
            this.cleanupStrategy = cleanupStrategy;
            this.initialDataSets = initialDataSets;
        }

        @Override
        public void execute(final MongoDatabase connection) throws DbFeatureException {
            final CleanupStrategyExecutor<MongoDatabase, Document> executor = cleanupStrategy.provide(provider);
            executor.execute(connection, initialDataSets);
        }
    }

    private static class ApplyCustomScriptFeature implements DbFeature<MongoDatabase> {

        private final List<String> scriptPaths;

        private ApplyCustomScriptFeature(final List<String> scriptPaths) {
            this.scriptPaths = scriptPaths;
        }

        @Override
        public void execute(final MongoDatabase connection) throws DbFeatureException {
            try {
                for (final String scriptPath : scriptPaths) {
                    executeScript(loadScript(scriptPath), connection);
                }
            } catch (final SQLException | IOException | URISyntaxException e) {
                throw new DbFeatureException("Could not apply custom scripts feature", e);
            }
        }

        private String loadScript(final String path) throws IOException, URISyntaxException {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            return new String(Files.readAllBytes(Paths.get(url.toURI())));
        }

        private void executeScript(final String script, final MongoDatabase connection) throws SQLException {
            final Document command = Document.parse(script);
            connection.runCommand(command);
        }
    }

    private static class SeedDataFeature implements DbFeature<MongoDatabase> {

        private final DataSeedStrategy dataSeedStrategy;
        private final List<Document> initialDataSets;
        private final DataSeedStrategyProvider provider;

        public SeedDataFeature(final DataSeedStrategyProvider provider, final DataSeedStrategy dataSeedStrategy,
                final List<Document> initialDataSets) {
            this.dataSeedStrategy = dataSeedStrategy;
            this.initialDataSets = initialDataSets;
            this.provider = provider;
        }

        @Override
        public void execute(final MongoDatabase connection) throws DbFeatureException {

        }
    }

    private static class VerifyDataAfterFeature implements DbFeature<MongoDatabase> {

        private ExpectedDataSets expectedDataSets;

        public VerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
            this.expectedDataSets = expectedDataSets;
        }

        @Override
        public void execute(final MongoDatabase connection) throws DbFeatureException {

        }
    }
}
