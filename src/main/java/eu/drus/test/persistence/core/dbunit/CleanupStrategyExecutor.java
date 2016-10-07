package eu.drus.test.persistence.core.dbunit;

public interface CleanupStrategyExecutor {
    void cleanupDatabase(String... tablesToExclude);
}
