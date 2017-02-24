package eu.drus.jpa.unit.fixture.dbunit;

import java.sql.SQLException;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.operation.DatabaseOperation;

import eu.drus.jpa.unit.api.CleanupStrategy.StrategyProvider;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor> {

    private static final String UNABLE_TO_CLEAN_DATABASE = "Unable to clean database.";

    @Override
    public CleanupStrategyExecutor strictStrategy() {
        return (final IDatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) -> {
            try {
                IDataSet dataSet = excludeTables(connection.createDataSet(), tablesToExclude);
                dataSet = new FilteredDataSet(new DatabaseSequenceFilter(connection), dataSet);
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
                IDataSet dataSet = excludeTables(mergeDataSets(initialDataSets), tablesToExclude);
                dataSet = new FilteredDataSet(new DatabaseSequenceFilter(connection), dataSet);
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
