package eu.drus.jpa.unit.annotation;

public enum CleanupPhase {
    BEFORE {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.beforeStrategy();
        }
    },
    AFTER {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.afterStrategy();
        }
    },
    NONE {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.noneStrategy();
        }
    };

    public abstract <T> T provide(StrategyProvider<T> provider);

    public interface StrategyProvider<T> {
        T beforeStrategy();

        T afterStrategy();

        T noneStrategy();
    }
}
