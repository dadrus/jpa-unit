package eu.drus.test.persistence.core.dbunit;

import java.util.List;

import org.dbunit.database.DatabaseConnection;
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
        return new StrictCleanupStrategyExecutor();
    }

    @Override
    public CleanupStrategyExecutor usedTablesOnlyStrategy() {
        return new UsedTablesOnlyCleanupStrategyExecutor();
    }

    @Override
    public CleanupStrategyExecutor usedRowsOnlyStrategy() {
        return new UsedRowsOnlyCleanupStrategyExecutor();
    }

    private IDataSet mergeDataSets(final List<IDataSet> dataSets) throws DataSetException {
        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
    }

    private IDataSet excludeTables(final IDataSet dataSet, final String... tablesToExclude) {
        return new FilteredDataSet(new ExcludeTableFilter(tablesToExclude), dataSet);
    }

    private class StrictCleanupStrategyExecutor implements CleanupStrategyExecutor {

        @Override
        public void execute(final DatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) {
            try {
                final IDataSet dataSet = excludeTables(connection.createDataSet(), tablesToExclude);
                DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
            } catch (final Exception e) {
                throw new RuntimeException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        }

    }

    private class UsedTablesOnlyCleanupStrategyExecutor implements CleanupStrategyExecutor {

        @Override
        public void execute(final DatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) {
            if (initialDataSets.isEmpty()) {
                return;
            }

            try {
                final IDataSet dataSet = excludeTables(mergeDataSets(initialDataSets), tablesToExclude);
                DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
            } catch (final Exception e) {
                throw new RuntimeException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        }

    }

    private class UsedRowsOnlyCleanupStrategyExecutor implements CleanupStrategyExecutor {

        @Override
        public void execute(final DatabaseConnection connection, final List<IDataSet> initialDataSets, final String... tablesToExclude) {
            if (initialDataSets.isEmpty()) {
                return;
            }

            try {
                final IDataSet dataSet = excludeTables(mergeDataSets(initialDataSets), tablesToExclude);
                DatabaseOperation.DELETE.execute(connection, dataSet);
            } catch (final Exception e) {
                throw new RuntimeException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        }
    }

}
