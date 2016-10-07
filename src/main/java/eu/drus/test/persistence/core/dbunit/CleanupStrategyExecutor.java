package eu.drus.test.persistence.core.dbunit;

import java.util.List;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;

public interface CleanupStrategyExecutor {
    void execute(final DatabaseConnection connection, final List<IDataSet> initialDataSets, String... tablesToExclude);
}
