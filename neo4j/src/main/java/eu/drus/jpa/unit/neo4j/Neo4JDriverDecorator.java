package eu.drus.jpa.unit.neo4j;

import com.zaxxer.hikari.HikariDataSource;

import eu.drus.jpa.unit.neo4j.ext.Configuration;
import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;

public class Neo4JDriverDecorator implements TestClassDecorator {

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
    public void beforeAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final Configuration configuration = configurationRegistry.getConfiguration(context.getDescriptor());
        context.storeData(Constants.KEY_DATA_SOURCE, configuration.createDataSource());
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final HikariDataSource ds = (HikariDataSource) context.getData(Constants.KEY_DATA_SOURCE);
        ds.close();
        context.storeData(Constants.KEY_DATA_SOURCE, null);
    }

}
