package eu.drus.jpa.unit.api;

public enum DataSeedStrategy {
    /**
     * Performs insert of the data defined in provided data sets. Default strategy.
     */
    INSERT {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.insertStrategy();
        }
    },

    /**
     * Performs insert of the data defined in provided data sets, after removal of all data present
     * in the tables referred in provided files.
     */
    CLEAN_INSERT {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.cleanInsertStrategy();
        }
    },

    /**
     * During this operation existing rows are updated and new ones are inserted. Entries already
     * existing in the database which are not defined in the provided dataset are not affected.
     */
    REFRESH {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.refreshStrategy();
        }
    },
    /**
     * This strategy updates existing rows using data provided in the datasets. If dataset contain a
     * row which is not present in the database (identified by its primary key) then exception is
     * thrown.
     */
    UPDATE {
        @Override
        public <T> T provide(final StrategyProvider<T> provider) {
            return provider.updateStrategy();
        }
    };

    public abstract <T> T provide(StrategyProvider<T> provider);

    public interface StrategyProvider<T> {
        T insertStrategy();

        T cleanInsertStrategy();

        T refreshStrategy();

        T updateStrategy();
    }
}
