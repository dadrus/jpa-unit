package eu.drus.jpa.unit.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.cassandra.ext.Configuration;
import eu.drus.jpa.unit.cassandra.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class CassandraSessionDecorator implements TestMethodDecorator {

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final Configuration config = configurationRegistry.getConfiguration(invocation.getContext().getDescriptor());
        final Cluster cluster = (Cluster) invocation.getContext().getData("cluster");
        final Session session = cluster.connect(config.getKeySpace());

        invocation.getContext().storeData("session", session);

        final CassandraFeatureExecutor featureExecutor = new CassandraFeatureExecutor(invocation.getFeatureResolver());
        featureExecutor.executeBeforeTest(session);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final CassandraFeatureExecutor featureExecutor = new CassandraFeatureExecutor(invocation.getFeatureResolver());

        try (final Session session = (Session) invocation.getContext().getData("session")) {
            featureExecutor.executeAfterTest(session, invocation.hasErrors());
        } finally {
            invocation.getContext().storeData("session", null);
        }
    }

}
