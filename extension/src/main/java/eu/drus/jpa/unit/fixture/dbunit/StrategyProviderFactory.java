package eu.drus.jpa.unit.fixture.dbunit;

public class StrategyProviderFactory {

    public CleanupStrategyProvider createCleanupStrategyProvider() {
        return new CleanupStrategyProvider();
    }

    public DataSeedStrategyProvider createDataSeedStrategyProvider() {
        return new DataSeedStrategyProvider();
    }
}
