package eu.drus.jpa.unit.sql.dbunit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.operation.DatabaseOperation;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.AbstractDbFeatureExecutor;
import eu.drus.jpa.unit.spi.AssertionErrorCollector;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DataSetFormat;
import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.sql.dbunit.dataset.DataSetLoaderProvider;

public class SqlDbFeatureExecutor extends AbstractDbFeatureExecutor<IDataSet, IDatabaseConnection> {

    private StrategyProviderFactory providerFactory;

    public SqlDbFeatureExecutor(final FeatureResolver featureResolver) {
        super(featureResolver);
        providerFactory = new StrategyProviderFactory();
    }

    // for tests
    void setProviderFactory(final StrategyProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    private static IDataSet mergeDataSets(final List<IDataSet> dataSets) throws DataSetException {
        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
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
    protected List<IDataSet> loadDataSets(final List<String> paths) {
        final List<IDataSet> dataSets = new ArrayList<>();
        try {
            for (final String path : paths) {
                final File file = new File(toUri(path));
                final DataSetLoader<IDataSet> loader = DataSetFormat.inferFromFile(file).select(new DataSetLoaderProvider());
                dataSets.add(loader.load(file));
            }
        } catch (final IOException e) {
            throw new JpaUnitException("Could not load initial data sets", e);
        }
        return dataSets;
    }

    @Override
    protected DbFeature<IDatabaseConnection> createCleanupFeature(final CleanupStrategy cleanupStrategy,
            final List<IDataSet> initialDataSets) {
        return (final IDatabaseConnection connection) -> {
            final CleanupStrategyExecutor<IDatabaseConnection, IDataSet> executor = cleanupStrategy
                    .provide(providerFactory.createCleanupStrategyProvider());
            executor.execute(connection, initialDataSets);
        };
    }

    @Override
    protected DbFeature<IDatabaseConnection> createApplyCustomScriptFeature(final List<String> scriptPaths) {
        return (final IDatabaseConnection connection) -> {
            try {
                for (final String scriptPath : scriptPaths) {
                    executeScript(loadScript(scriptPath), connection.getConnection());
                }
            } catch (final SQLException | IOException | URISyntaxException e) {
                throw new DbFeatureException("Could not apply custom scripts feature", e);
            }
        };
    }

    @Override
    protected DbFeature<IDatabaseConnection> createSeedDataFeature(final DataSeedStrategy dataSeedStrategy,
            final List<IDataSet> initialDataSets) {
        return (final IDatabaseConnection connection) -> {
            try {
                final DatabaseOperation operation = dataSeedStrategy.provide(providerFactory.createDataSeedStrategyProvider());
                operation.execute(connection, mergeDataSets(initialDataSets));
            } catch (DatabaseUnitException | SQLException e) {
                throw new DbFeatureException("Could not execute DB seed feature", e);
            }
        };
    }

    @Override
    protected DbFeature<IDatabaseConnection> createVerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
        return (final IDatabaseConnection connection) -> {
            try {
                final IDataSet currentDataSet = connection.createDataSet();
                final IDataSet expectedDataSet = mergeDataSets(loadDataSets(Arrays.asList(expectedDataSets.value())));

                final DataSetComparator dataSetComparator = new DataSetComparator(expectedDataSets.orderBy(),
                        expectedDataSets.excludeColumns(), expectedDataSets.strict(), getColumnFilter(expectedDataSets));

                final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
                dataSetComparator.compare(currentDataSet, expectedDataSet, errorCollector);

                errorCollector.report();
            } catch (final SQLException | DatabaseUnitException e) {
                throw new DbFeatureException("Could not execute DB contents verification feature", e);
            }
        };
    }

    private HashSet<Class<? extends IColumnFilter>> getColumnFilter(final ExpectedDataSets expectedDataSets) {
        final Class<? extends IColumnFilter>[] filter = (Class<? extends IColumnFilter>[]) expectedDataSets.filter();
        return filter == null ? new HashSet<>() : new HashSet<>(Arrays.asList(filter));
    }

    private void executeScript(final String script, final Connection connection) throws SQLException {
        for (final String sqlStatement : new SqlScript(script)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sqlStatement);
            }
        }
    }
}
