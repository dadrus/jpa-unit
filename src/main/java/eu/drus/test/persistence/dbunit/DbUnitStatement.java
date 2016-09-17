package eu.drus.test.persistence.dbunit;

import static eu.drus.test.persistence.dbunit.DataSetUtils.mergeDataSets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.core.AssertionErrorCollector;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.sql.AnsiSqlStatementSplitter;
import eu.drus.test.persistence.dbunit.cleanup.CleanupStrategyExecutor;
import eu.drus.test.persistence.dbunit.cleanup.CleanupStrategyProvider;
import eu.drus.test.persistence.dbunit.dataset.DataSetBuilder;

public class DbUnitStatement extends Statement {

    private final Map<String, Object> properties;
    private final Statement base;
    private final List<IDataSet> initialDataSets;
    private final FeatureResolver featureResolver;

    DbUnitStatement(final Map<String, Object> properties, final FeatureResolver featureResolver, final Statement base) {
        this.properties = properties;
        this.featureResolver = featureResolver;
        this.base = base;

        initialDataSets = loadDataSets(featureResolver.getSeedData());
    }

    @Override
    public void evaluate() throws Throwable {
        try (Connection conn = openConnection(properties)) {
            final DatabaseConnection connection = new DatabaseConnection(conn);

            if (featureResolver.shouldCleanupBefore()) {
                cleanup(true, connection);
            }

            if (featureResolver.shouldCleanupUsingScriptBefore()) {
                applyScripts(true, featureResolver.getCleanupScripts(), connection);
            }

            if (featureResolver.shouldApplyCustomScriptBefore()) {
                applyScripts(true, featureResolver.getPostExecutionScripts(), connection);
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
                    applyScripts(isComplete, featureResolver.getCleanupScripts(), connection);
                }

                if (featureResolver.shouldApplyCustomScriptAfter()) {
                    applyScripts(isComplete, featureResolver.getPostExecutionScripts(), connection);
                }
            }
        }
    }

    private void applyScripts(final boolean propageExceptions, final List<String> scriptPaths, final DatabaseConnection connection)
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
        final ExpectedDataSets dataSetsToVerify = featureResolver.getExpectedDataSets();

        final IDataSet currentDataSet = connection.createDataSet();
        final IDataSet expectedDataSet = mergeDataSets(loadDataSets(Arrays.asList(dataSetsToVerify.value())));

        final DataSetComparator dataSetComparator = new DataSetComparator(dataSetsToVerify.orderBy(), dataSetsToVerify.excludeColumns(),
                featureResolver.getCustomColumnFilter());

        final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
        dataSetComparator.compare(currentDataSet, expectedDataSet, errorCollector);

        errorCollector.report();
    }

    private List<IDataSet> loadDataSets(final List<String> paths) {
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
