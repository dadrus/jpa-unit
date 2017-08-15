package eu.drus.jpa.unit.sql.dbunit;

public class StrategyProviderFactory {

    public CleanupStrategyProvider createCleanupStrategyProvider() {
        return new CleanupStrategyProvider();
    }

    public DataSeedStrategyProvider createDataSeedStrategyProvider() {
        return new DataSeedStrategyProvider();
    }
}
