package eu.drus.test.persistence.core.metadata;

import java.lang.reflect.Method;

import org.junit.runners.model.TestClass;

import eu.drus.test.persistence.annotation.Cleanup;
import eu.drus.test.persistence.annotation.CleanupPhase;
import eu.drus.test.persistence.annotation.CleanupStrategy;
import eu.drus.test.persistence.annotation.CleanupUsingScripts;
import eu.drus.test.persistence.annotation.DataSeedStrategy;
import eu.drus.test.persistence.annotation.InitialDataSets;
import eu.drus.test.persistence.annotation.TransactionMode;
import eu.drus.test.persistence.annotation.Transactional;

public class FeatureResolver {

    private final MetadataExtractor metadataExtractor;

    private final Method testMethod;

    FeatureResolver(final Method testMethod, final Class<?> clazz) {
        metadataExtractor = new MetadataExtractor(new TestClass(clazz));
        this.testMethod = testMethod;
    }

    public CleanupStrategy getCleanupStrategy() {
        final Cleanup cleanup = metadataExtractor.cleanup().fetchUsingFirst(testMethod);
        return cleanup == null ? CleanupStrategy.STRICT : cleanup.strategy();
    }

    public DataSeedStrategy getDataSeedStrategy() {
        final InitialDataSets initialDataSet = metadataExtractor.initialDataSets().fetchUsingFirst(testMethod);
        return initialDataSet == null ? DataSeedStrategy.INSERT : initialDataSet.seedStrategy();
    }

    public TransactionMode getTransactionMode() {
        final Transactional transactional = metadataExtractor.transactional().fetchUsingFirst(testMethod);
        return transactional == null ? TransactionMode.COMMIT : transactional.value();
    }

    public boolean shouldSeedData() {
        return metadataExtractor.initialDataSets().isDefinedOnClassLevel()
                || metadataExtractor.initialDataSets().isDefinedOnMethod(testMethod);
    }

    public boolean shouldApplyCustomScriptBefore() {
        return metadataExtractor.applyScriptsBefore().isDefinedOnClassLevel()
                || metadataExtractor.applyScriptsBefore().isDefinedOnMethod(testMethod);
    }

    public boolean shouldApplyCustomScriptAfter() {
        return metadataExtractor.applyScriptsAfter().isDefinedOnClassLevel()
                || metadataExtractor.applyScriptsAfter().isDefinedOnMethod(testMethod);
    }

    public boolean shouldVerifyDataAfter() {
        return metadataExtractor.expectedDataSets().isDefinedOnClassLevel()
                || metadataExtractor.expectedDataSets().isDefinedOnMethod(testMethod);
    }

    public boolean shouldCleanupBefore() {
        return shouldCleanup() && getCleanupPhase() == CleanupPhase.BEFORE;
    }

    public boolean shouldCleanupAfter() {
        return shouldCleanup() && getCleanupPhase() == CleanupPhase.AFTER;
    }

    private boolean shouldCleanup() {
        final Cleanup cleanup = metadataExtractor.cleanup().fetchUsingFirst(testMethod);
        return cleanup == null || cleanup.phase() != CleanupPhase.NONE;
    }

    private CleanupPhase getCleanupPhase() {
        final Cleanup cleanup = metadataExtractor.cleanup().fetchUsingFirst(testMethod);
        return cleanup == null ? CleanupPhase.AFTER : cleanup.phase();
    }

    public boolean shouldCleanupUsingScriptBefore() {
        return shouldCleanupUsingScript() && getCleanupUsingScriptPhase() == CleanupPhase.BEFORE;
    }

    public boolean shouldCleanupUsingScriptAfter() {
        return shouldCleanupUsingScript() && getCleanupUsingScriptPhase() == CleanupPhase.AFTER;
    }

    private boolean shouldCleanupUsingScript() {
        final CleanupUsingScripts cleanupUsingScript = metadataExtractor.cleanupUsingScripts().fetchUsingFirst(testMethod);
        return cleanupUsingScript == null ? false : cleanupUsingScript.phase() != CleanupPhase.NONE;
    }

    private CleanupPhase getCleanupUsingScriptPhase() {
        final CleanupUsingScripts cleanupUsingScript = metadataExtractor.cleanupUsingScripts().fetchUsingFirst(testMethod);
        return cleanupUsingScript == null ? CleanupPhase.AFTER : cleanupUsingScript.phase();
    }
}
