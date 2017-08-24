package eu.drus.jpa.unit.cassandra;

import com.datastax.driver.core.Cluster;

import eu.drus.jpa.unit.cassandra.ext.Configuration;
import eu.drus.jpa.unit.cassandra.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class CassandraClusterDecorator implements TestClassDecorator {

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final Configuration config = configurationRegistry.getConfiguration(ctx.getDescriptor());
        ctx.storeData("cluster", config.openCluster());
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final Cluster cluster = (Cluster) ctx.getData("cluster");
        cluster.close();
        ctx.storeData("cluster", null);
    }

}
