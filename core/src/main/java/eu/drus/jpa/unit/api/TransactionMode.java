package eu.drus.jpa.unit.api;

public enum TransactionMode {
    ROLLBACK {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.rollbackStrategy();
        }
    },
    COMMIT {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.commitStrategy();
        }
    },
    DISABLED {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.disabledStrategy();
        }
    };

    public abstract <T> T provide(StrategyProvider<T> provider);

    public interface StrategyProvider<T> {
        T rollbackStrategy();

        T commitStrategy();

        T disabledStrategy();
    }
}
