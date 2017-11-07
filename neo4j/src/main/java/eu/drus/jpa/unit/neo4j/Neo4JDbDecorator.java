package eu.drus.jpa.unit.neo4j;

import java.sql.Connection;

import javax.sql.DataSource;

import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class Neo4JDbDecorator implements TestMethodDecorator {

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
    public void beforeTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final Neo4JDbFeatureExecutor dbFeatureExecutor = new Neo4JDbFeatureExecutor(invocation.getFeatureResolver(),
                context.getDescriptor().getClasses());
        final DataSource ds = (DataSource) context.getData(Constants.KEY_DATA_SOURCE);
        final Connection connection = ds.getConnection();
        connection.setAutoCommit(false);
        context.storeData(Constants.KEY_CONNECTION, connection);
        dbFeatureExecutor.executeBeforeTest(connection);
    }

    @Override
    public void afterTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final Neo4JDbFeatureExecutor dbFeatureExecutor = new Neo4JDbFeatureExecutor(invocation.getFeatureResolver(),
                context.getDescriptor().getClasses());
        try (final Connection connection = (Connection) context.getData(Constants.KEY_CONNECTION)) {
            dbFeatureExecutor.executeAfterTest(connection, invocation.getException().isPresent());
        } finally {
            context.storeData(Constants.KEY_CONNECTION, null);
        }

    }

}
