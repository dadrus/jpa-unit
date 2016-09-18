package eu.drus.test.persistence.core.dbunit.cleanup;

public interface CleanupStrategyExecutor {
    void cleanupDatabase(String... tablesToExclude);
}
