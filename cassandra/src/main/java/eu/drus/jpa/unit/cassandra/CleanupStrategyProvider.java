package eu.drus.jpa.unit.cassandra;

import java.util.List;

import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.api.CleanupStrategy.StrategyProvider;
import eu.drus.jpa.unit.cassandra.dataset.CompositeDataSet;
import eu.drus.jpa.unit.cassandra.dataset.DataBaseDataSet;
import eu.drus.jpa.unit.cassandra.dataset.DataSet;
import eu.drus.jpa.unit.cassandra.dataset.FilteredDataSet;
import eu.drus.jpa.unit.cassandra.operation.CassandraOperations;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor<Session, DataSet>> {

    @Override
    public CleanupStrategyExecutor<Session, DataSet> strictStrategy() {
        return (final Session session, final List<DataSet> initialDataSets, final String... tablesToExclude) -> {
            final DataSet ds = excludeTables(new DataBaseDataSet(session), tablesToExclude);
            CassandraOperations.DELETE_ALL.execute(session, ds);
        };
    }

    @Override
    public CleanupStrategyExecutor<Session, DataSet> usedTablesOnlyStrategy() {
        return (final Session session, final List<DataSet> initialDataSets, final String... tablesToExclude) -> {
            if (initialDataSets.isEmpty()) {
                return;
            }

            final DataSet ds = excludeTables(new CompositeDataSet(initialDataSets), tablesToExclude);
            CassandraOperations.DELETE_ALL.execute(session, ds);
        };
    }

    @Override
    public CleanupStrategyExecutor<Session, DataSet> usedRowsOnlyStrategy() {
        return (final Session session, final List<DataSet> initialDataSets, final String... tablesToExclude) -> {
            if (initialDataSets.isEmpty()) {
                return;
            }

            final DataSet ds = excludeTables(new CompositeDataSet(initialDataSets), tablesToExclude);
            CassandraOperations.DELETE.execute(session, ds);
        };
    }

    private DataSet excludeTables(final DataSet dataSet, final String... tablesToExclude) {
        return new FilteredDataSet(dataSet, table -> {
            final String name = table.getTableProperties().getTableName();
            for (final String otherName : tablesToExclude) {
                if (otherName.equals(name)) {
                    return false;
                }
            }
            return true;
        });
    }

}
