package eu.drus.test.persistence.dbunit.cleanup;

public interface CleanupStrategyExecutor {
    void cleanupDatabase(String... tablesToExclude);
}
