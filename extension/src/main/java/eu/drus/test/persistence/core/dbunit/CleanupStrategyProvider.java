package eu.drus.test.persistence.core.dbunit;

import java.sql.SQLException;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.operation.DatabaseOperation;

import eu.drus.test.persistence.annotation.CleanupStrategy.StrategyProvider;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor> {

    private static final String UNABLE_TO_CLEAN_DATABASE = "Unable to clean database.";

    @Override
    public CleanupStrategyExecutor strictStrategy() {
        return (final IDatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) -> {
            try {
                final IDataSet dataSet = excludeTables(connection.createDataSet(), tablesToExclude);
                DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
            } catch (final SQLException | DatabaseUnitException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    @Override
    public CleanupStrategyExecutor usedTablesOnlyStrategy() {
        return (final IDatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) -> {
            if (initialDataSets.isEmpty()) {
                return;
            }

            try {
                final IDataSet dataSet = excludeTables(mergeDataSets(initialDataSets), tablesToExclude);
                DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
            } catch (final SQLException | DatabaseUnitException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    @Override
    public CleanupStrategyExecutor usedRowsOnlyStrategy() {
        return (final IDatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) -> {
            if (initialDataSets.isEmpty()) {
                return;
            }

            try {
                final IDataSet dataSet = excludeTables(mergeDataSets(initialDataSets), tablesToExclude);
                DatabaseOperation.DELETE.execute(connection, dataSet);
            } catch (final SQLException | DatabaseUnitException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    private IDataSet mergeDataSets(final List<IDataSet> dataSets) throws DataSetException {
        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
    }

    private IDataSet excludeTables(final IDataSet dataSet, final String... tablesToExclude) {
        return new FilteredDataSet(new ExcludeTableFilter(tablesToExclude), dataSet);
    }
}
