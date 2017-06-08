package eu.drus.jpa.unit.spi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;

public abstract class AbstractDbFeatureFactory<D, C> {

    private final FeatureResolver featureResolver;
    private List<D> initialDataSets;

    protected AbstractDbFeatureFactory(final FeatureResolver featureResolver) {
        this.featureResolver = featureResolver;
    }

    protected abstract List<D> loadDataSets(final List<String> paths);

    protected abstract DbFeature<C> createCleanupFeature(CleanupStrategy cleanupStrategy, List<D> initialDataSets);

    protected abstract DbFeature<C> createApplyCustomScriptFeature(List<String> scriptPaths);

    protected abstract DbFeature<C> createSeedDataFeature(DataSeedStrategy dataSeedStrategy, List<D> initialDataSets);

    protected abstract DbFeature<C> createVerifyDataAfterFeature(ExpectedDataSets expectedDataSets);

    protected String loadScript(final String path) throws IOException, URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new FileNotFoundException(path + " not found");
        }
        return new String(Files.readAllBytes(Paths.get(url.toURI())));
    }

    private List<D> getInitialDataSets() {
        if (initialDataSets == null) {
            initialDataSets = loadDataSets(featureResolver.getSeedData());
        }
        return initialDataSets;
    }

    public DbFeature<C> getCleanUpBeforeFeature() {
        if (featureResolver.shouldCleanupBefore()) {
            return createCleanupFeature(featureResolver.getCleanupStrategy(), getInitialDataSets());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getCleanUpAfterFeature() {
        if (featureResolver.shouldCleanupAfter()) {
            return createCleanupFeature(featureResolver.getCleanupStrategy(), getInitialDataSets());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getCleanupUsingScriptBeforeFeature() {
        if (featureResolver.shouldCleanupUsingScriptBefore()) {
            return createApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getCleanupUsingScriptAfterFeature() {
        if (featureResolver.shouldCleanupUsingScriptAfter()) {
            return createApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getApplyCustomScriptBeforeFeature() {
        if (featureResolver.shouldApplyCustomScriptBefore()) {
            return createApplyCustomScriptFeature(featureResolver.getPreExecutionScripts());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getApplyCustomScriptAfterFeature() {
        if (featureResolver.shouldApplyCustomScriptAfter()) {
            return createApplyCustomScriptFeature(featureResolver.getPostExecutionScripts());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getSeedDataFeature() {
        if (featureResolver.shouldSeedData()) {
            return createSeedDataFeature(featureResolver.getDataSeedStrategy(), getInitialDataSets());
        } else {
            return new NopFeature();
        }
    }

    public DbFeature<C> getVerifyDataAfterFeature() {
        if (featureResolver.shouldVerifyDataAfter()) {
            return createVerifyDataAfterFeature(featureResolver.getExpectedDataSets());
        } else {
            return new NopFeature();
        }
    }

    private class NopFeature implements DbFeature<C> {

        @Override
        public void execute(final C connection) throws DbFeatureException {
            // does nothing like the name implies
        }
    }
}
