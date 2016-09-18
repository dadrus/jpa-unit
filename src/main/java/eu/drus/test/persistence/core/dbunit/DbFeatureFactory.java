package eu.drus.test.persistence.core.dbunit;

import static eu.drus.test.persistence.core.dbunit.DataSetUtils.mergeDataSets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.operation.DatabaseOperation;

import eu.drus.test.persistence.annotation.CleanupStrategy;
import eu.drus.test.persistence.annotation.DataSeedStrategy;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.core.AssertionErrorCollector;
import eu.drus.test.persistence.core.dbunit.cleanup.CleanupStrategyExecutor;
import eu.drus.test.persistence.core.dbunit.cleanup.CleanupStrategyProvider;
import eu.drus.test.persistence.core.dbunit.dataset.DataSetBuilder;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.sql.AnsiSqlStatementSplitter;

public class DbFeatureFactory {

    private static class NopFeature implements DbFeature {

        @Override
        public void execute(final DatabaseConnection connection) throws DbFeatureException {}
    }

    private static class CleanupFeature implements DbFeature {

        private final CleanupStrategy cleanupStrategy;
        private final List<IDataSet> initialDataSets;

        private CleanupFeature(final CleanupStrategy cleanupStrategy, final List<IDataSet> initialDataSets) {
            this.cleanupStrategy = cleanupStrategy;
            this.initialDataSets = initialDataSets;
        }

        @Override
        public void execute(final DatabaseConnection connection) throws DbFeatureException {
            final CleanupStrategyExecutor executor = cleanupStrategy.provide(new CleanupStrategyProvider(connection, initialDataSets));
            executor.cleanupDatabase();
        }
    }

    private static class ApplyCustomScriptFeature implements DbFeature {

        private final List<String> scriptPaths;

        private ApplyCustomScriptFeature(final List<String> scriptPaths) {
            this.scriptPaths = scriptPaths;
        }

        @Override
        public void execute(final DatabaseConnection connection) throws DbFeatureException {
            try {
                for (final String script : scriptPaths) {
                    executeScript(script, connection.getConnection());
                }
            } catch (final SQLException e) {
                throw new DbFeatureException("Could not apply custom scripts feature", e);
            }
        }

        private void executeScript(final String script, final Connection connection) throws SQLException {
            final AnsiSqlStatementSplitter splitter = new AnsiSqlStatementSplitter();
            final List<String> statements = splitter.splitStatements(script);

            for (final String sqlStatement : statements) {
                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute(sqlStatement);
                }
            }
        }
    }

    private static class SeedDataFeature implements DbFeature {

        private final DataSeedStrategy dataSeedStrategy;
        private final List<IDataSet> initialDataSets;

        public SeedDataFeature(final DataSeedStrategy dataSeedStrategy, final List<IDataSet> initialDataSets) {
            this.dataSeedStrategy = dataSeedStrategy;
            this.initialDataSets = initialDataSets;
        }

        @Override
        public void execute(final DatabaseConnection connection) throws DbFeatureException {
            try {
                final DatabaseOperation operation = dataSeedStrategy.provide(new DataSeedStrategyProvider());
                operation.execute(connection, mergeDataSets(initialDataSets));
            } catch (DatabaseUnitException | SQLException e) {
                throw new DbFeatureException("Could not execute DB seed feature", e);
            }
        }
    }

    private static class VerifyDataAfterFeature implements DbFeature {

        private ExpectedDataSets expectedDataSets;
        private Set<Class<? extends IColumnFilter>> customColumnFilter;

        public VerifyDataAfterFeature(final ExpectedDataSets expectedDataSets,
                final Set<Class<? extends IColumnFilter>> customColumnFilter) {
            this.expectedDataSets = expectedDataSets;
            this.customColumnFilter = customColumnFilter;
        }

        @Override
        public void execute(final DatabaseConnection connection) throws DbFeatureException {
            try {
                final IDataSet currentDataSet = connection.createDataSet();
                final IDataSet expectedDataSet = mergeDataSets(loadDataSets(Arrays.asList(expectedDataSets.value())));

                final DataSetComparator dataSetComparator = new DataSetComparator(expectedDataSets.orderBy(),
                        expectedDataSets.excludeColumns(), customColumnFilter);

                final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
                dataSetComparator.compare(currentDataSet, expectedDataSet, errorCollector);

                errorCollector.report();
            } catch (final SQLException | DatabaseUnitException | ReflectiveOperationException e) {
                throw new DbFeatureException("Could not execute DB contents verification feature", e);
            }
        }

        private List<IDataSet> loadDataSets(final List<String> paths) {
            final List<IDataSet> dataSets = new ArrayList<>();
            for (final String path : paths) {
                final DataSetBuilder builder = DataSetBuilder.builderFor(DataSetFormat.inferFromFile(path));
                dataSets.add(builder.build(path));
            }
            return dataSets;
        }

    }

    private static final NopFeature NOP_FEATURE = new NopFeature();

    private FeatureResolver featureResolver;
    private final List<IDataSet> initialDataSets;

    public DbFeatureFactory(final FeatureResolver featureResolver) {
        this.featureResolver = featureResolver;
        initialDataSets = loadDataSets(featureResolver.getSeedData());
    }

    private List<IDataSet> loadDataSets(final List<String> paths) {
        final List<IDataSet> dataSets = new ArrayList<>();
        for (final String path : paths) {
            final DataSetBuilder builder = DataSetBuilder.builderFor(DataSetFormat.inferFromFile(path));
            dataSets.add(builder.build(path));
        }
        return dataSets;
    }

    public DbFeature getCleanUpBeforeFeature() {
        if (featureResolver.shouldCleanupBefore()) {
            return new CleanupFeature(featureResolver.getCleanupStrategy(), initialDataSets);
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getCleanUpAfterFeature() {
        if (featureResolver.shouldCleanupAfter()) {
            return new CleanupFeature(featureResolver.getCleanupStrategy(), initialDataSets);
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getCleanupUsingScriptBeforeFeature() {
        if (featureResolver.shouldCleanupUsingScriptBefore()) {
            return new ApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getCleanupUsingScriptAfterFeature() {
        if (featureResolver.shouldCleanupUsingScriptAfter()) {
            return new ApplyCustomScriptFeature(featureResolver.getCleanupScripts());
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getApplyCustomScriptBeforeFeature() {
        if (featureResolver.shouldApplyCustomScriptBefore()) {
            return new ApplyCustomScriptFeature(featureResolver.getPostExecutionScripts());
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getApplyCustomScriptAfterFeature() {
        if (featureResolver.shouldApplyCustomScriptBefore()) {
            return new ApplyCustomScriptFeature(featureResolver.getPostExecutionScripts());
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getSeedDataFeature() {
        if (featureResolver.shouldSeedData()) {
            return new SeedDataFeature(featureResolver.getDataSeedStrategy(), initialDataSets);
        } else {
            return NOP_FEATURE;
        }
    }

    public DbFeature getVerifyDataAfterFeature() {
        if (featureResolver.shouldVerifyDataAfter()) {
            return new VerifyDataAfterFeature(featureResolver.getExpectedDataSets(), featureResolver.getCustomColumnFilter());
        } else {
            return NOP_FEATURE;
        }
    }
}
