package eu.drus.test.persistence.core.dbunit;

public class StrategyProviderFactory {

    public CleanupStrategyProvider createCleanupStrategyProvider() {
        return new CleanupStrategyProvider();
    }

    public DataSeedStrategyProvider createDataSeedStrategyProvider() {
        return new DataSeedStrategyProvider();
    }
}
