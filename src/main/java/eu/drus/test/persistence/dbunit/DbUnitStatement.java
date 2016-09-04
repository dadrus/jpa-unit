package eu.drus.test.persistence.dbunit;

import static eu.drus.test.persistence.dbunit.DataSetUtils.mergeDataSets;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import eu.drus.test.persistence.annotation.ApplyScriptsAfter;
import eu.drus.test.persistence.annotation.ApplyScriptsBefore;
import eu.drus.test.persistence.annotation.CleanupUsingScripts;
import eu.drus.test.persistence.annotation.CustomColumnFilter;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.annotation.InitialDataSets;
import eu.drus.test.persistence.core.AssertionErrorCollector;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;
import eu.drus.test.persistence.core.metadata.MetadataExtractor;
import eu.drus.test.persistence.core.sql.AnsiSqlStatementSplitter;
import eu.drus.test.persistence.dbunit.cleanup.CleanupStrategyExecutor;
import eu.drus.test.persistence.dbunit.cleanup.CleanupStrategyProvider;
import eu.drus.test.persistence.dbunit.dataset.DataSetBuilder;

public class DbUnitStatement extends Statement {

    private final MetadataExtractor extractor;
    private final Method testMethod;
    private final Map<String, Object> properties;
    private final Statement base;
    private final List<IDataSet> initialDataSets;
    private final FeatureResolver featureResolver;

    DbUnitStatement(final Map<String, Object> properties, final Class<?> clazz, final Method testMethod, final Statement base) {
        this.testMethod = testMethod;
        this.properties = properties;
        this.base = base;

        extractor = new MetadataExtractor(new TestClass(clazz));
        featureResolver = new FeatureResolverFactory().createFeatureResolver(testMethod, clazz);
        initialDataSets = createInitialDataSets();
    }

    @Override
    public void evaluate() throws Throwable {
        try (Connection conn = openConnection(properties)) {
            final DatabaseConnection connection = new DatabaseConnection(conn);

            if (featureResolver.shouldCleanupBefore()) {
                cleanup(true, connection);
            }

            if (featureResolver.shouldCleanupUsingScriptBefore()) {
                final CleanupUsingScripts cleanupUsingScript = extractor.cleanupUsingScripts().fetchUsingFirst(testMethod);
                applyScripts(true, cleanupUsingScript.value(), connection);
            }

            if (featureResolver.shouldApplyCustomScriptBefore()) {
                final ApplyScriptsBefore applyScriptBefore = extractor.applyScriptsBefore().fetchUsingFirst(testMethod);
                applyScripts(true, applyScriptBefore.value(), connection);
            }

            if (featureResolver.shouldSeedData()) {
                final DatabaseOperation operation = featureResolver.getDataSeedStrategy().provide(new DataSeedStrategyProvider());
                operation.execute(connection, mergeDataSets(initialDataSets));
            }

            boolean isComplete = false;

            try {
                base.evaluate();

                if (featureResolver.shouldVerifyDataAfter()) {
                    verifyDatabase(connection);
                }
                isComplete = true;
            } finally {
                if (featureResolver.shouldCleanupAfter()) {
                    cleanup(isComplete, connection);
                }

                if (featureResolver.shouldCleanupUsingScriptAfter()) {
                    final CleanupUsingScripts cleanupUsingScript = extractor.cleanupUsingScripts().fetchUsingFirst(testMethod);
                    applyScripts(isComplete, cleanupUsingScript.value(), connection);
                }

                if (featureResolver.shouldApplyCustomScriptAfter()) {
                    final ApplyScriptsAfter applyScriptAfter = extractor.applyScriptsAfter().fetchUsingFirst(testMethod);
                    applyScripts(isComplete, applyScriptAfter.value(), connection);
                }
            }
        }
    }

    private void applyScripts(final boolean propageExceptions, final String[] scriptPaths, final DatabaseConnection connection)
            throws Exception {
        try {
            for (final String script : scriptPaths) {
                executeScript(script, connection.getConnection());
            }
        } catch (final Exception e) {
            if (propageExceptions) {
                throw e;
            }
            // TODO log
        }
    }

    private void cleanup(final boolean propageExceptions, final DatabaseConnection connection) {
        try {
            final CleanupStrategyExecutor executor = featureResolver.getCleanupStrategy()
                    .provide(new CleanupStrategyProvider(connection, initialDataSets));
            // TODO: add annotation for this
            executor.cleanupDatabase();
        } catch (final RuntimeException e) {
            if (propageExceptions) {
                throw e;
            }
            // TODO log
        }
    }

    private void verifyDatabase(final DatabaseConnection connection) throws Throwable {
        final ExpectedDataSets dataSetsToVerify = extractor.expectedDataSets().fetchUsingFirst(testMethod);
        final CustomColumnFilter customColumnFilter = extractor.using(CustomColumnFilter.class).fetchUsingFirst(testMethod);
        final Set<Class<? extends IColumnFilter>> filter = new HashSet<>();
        if (customColumnFilter != null) {
            filter.addAll(Arrays.asList(customColumnFilter.value()));
        }

        final IDataSet currentDataSet = connection.createDataSet();
        final IDataSet expectedDataSet = mergeDataSets(loadDataSets(dataSetsToVerify.value()));

        final DataSetComparator dataSetComparator = new DataSetComparator(dataSetsToVerify.orderBy(), dataSetsToVerify.excludeColumns(),
                filter);

        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        dataSetComparator.compare(currentDataSet, expectedDataSet, errorCollector);

        errorCollector.report();
    }

    private List<IDataSet> createInitialDataSets() {
        final InitialDataSets dataSet = extractor.initialDataSets().fetchUsingFirst(testMethod);
        return dataSet == null ? Collections.emptyList() : loadDataSets(dataSet.value());
    }

    private List<IDataSet> loadDataSets(final String[] paths) {
        final List<IDataSet> dataSets = new ArrayList<>();
        for (final String path : paths) {
            final DataSetBuilder builder = DataSetBuilder.builderFor(DataSetFormat.inferFromFile(path));
            dataSets.add(builder.build(path));
        }
        return dataSets;
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

    private Connection openConnection(final Map<String, Object> properties) throws SQLException, ClassNotFoundException {
        final String connectionUrl = (String) properties.get("javax.persistence.jdbc.url");
        final String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
        final String password = (String) properties.get("javax.persistence.jdbc.password");
        final String username = (String) properties.get("javax.persistence.jdbc.user");

        Class.forName(driverClass);

        if (username == null && password == null) {
            return DriverManager.getConnection(connectionUrl);
        } else {
            return DriverManager.getConnection(connectionUrl, username, password);
        }
    }
}
