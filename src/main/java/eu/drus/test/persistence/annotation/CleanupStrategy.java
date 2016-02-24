package eu.drus.test.persistence.annotation;

public enum CleanupStrategy {
    /**
     * Cleans entire database. Might require turning off database constraints (e.g. referential
     * integrity).
     */
    STRICT {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.strictStrategy();
        }
    },

    /**
     * Deletes only those entries which were defined in data sets.
     */
    USED_ROWS_ONLY {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.usedRowsOnlyStrategy();
        }
    },

    /**
     * Deletes only those tables which were used in data sets.
     */
    USED_TABLES_ONLY {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.usedTablesOnlyStrategy();
        }
    };

    public abstract <T> T provide(StrategyProvider<T> provider);

    public interface StrategyProvider<T> {
        T strictStrategy();

        T usedTablesOnlyStrategy();

        T usedRowsOnlyStrategy();
    }
}
